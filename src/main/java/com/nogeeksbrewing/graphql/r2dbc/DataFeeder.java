package com.nogeeksbrewing.graphql.r2dbc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nogeeksbrewing.graphql.json.Batch;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class DataFeeder {

    private static final Log LOG = LogFactory.getLog(DataFeeder.class);

    private final BatchRepository batchRepository;
    private final String url;
    private final String authToken;
    private final RestTemplate restTemplate = new RestTemplate();

    public DataFeeder(@Autowired BatchRepository batchRepository,
                      @Value("${noGeeksBrewing.brewFather.url}") String url,
                      @Value("${noGeeksBrewing.brewFather.authToken}") String authToken) {

        this.batchRepository = batchRepository;
        this.url = url;
        this.authToken = authToken;
    }

    @PostConstruct
    public void init() {
        LOG.info("URL: " + url);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    load();
                } catch (Exception ex) {
                    LOG.error("Unable to load data", ex);
                }
            }
        }, 5000, 60000);
    }

    private void load() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + authToken);

        String last = "";
        boolean keepGoing = true;

        while (keepGoing) {
            RequestEntity<String> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url + last));

            ResponseEntity<BrewfatherBatch[]> responseEntity = restTemplate.exchange(requestEntity,
                    BrewfatherBatch[].class);

            LOG.info("Found # result(s): "
                    + (responseEntity.getBody() != null ? responseEntity.getBody().length : null));

            if (responseEntity.getBody() != null) {
                for (BrewfatherBatch batch : responseEntity.getBody()) {
                    batchRepository.findByBrewfatherId(batch.id)
                            .defaultIfEmpty(batch(batch, null))
                            .subscribe(before -> {
                                LOG.info("Brewfather batch: " + batch);
                                LOG.info("No Geeks Brewing batch (before): " + before);
                                batchRepository.save(batch(batch, before.batchId()))
                                        .subscribe(after -> LOG.info("No Geeks Brewing batch (after): " + after));
                            });

                    last = batch.id;
                }
            }

            if (responseEntity.getBody() == null || responseEntity.getBody().length == 0) {
                keepGoing = false;
            }

            batchRepository.count().subscribe(count -> LOG.info("Repository size: " + count));
        }
    }

    private Batch batch(BrewfatherBatch batch, Long id) {
        return new Batch(id,
                batch.id,
                batch.name,
                batch.batchNumber,
                batch.brewDate,
                batch.status,
                batch.recipe.style.name,
                batch.share);
    }

    private record BrewfatherBatch(@JsonProperty("_id") String id,
                                   @JsonProperty String name,
                                   @JsonProperty Long brewDate,
                                   @JsonProperty String status,
                                   @JsonProperty("_share") String share,
                                   @JsonProperty("batchNo") Long batchNumber,
                                   @JsonProperty BrewfatherRecipe recipe) { }

    private record BrewfatherRecipe(@JsonProperty String name,
                                    @JsonProperty BrewfatherStyle style) { }

    private record BrewfatherStyle(@JsonProperty String name) { }

}
