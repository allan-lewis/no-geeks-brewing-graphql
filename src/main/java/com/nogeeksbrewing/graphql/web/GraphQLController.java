package com.nogeeksbrewing.graphql.web;

import com.nogeeksbrewing.graphql.json.Batch;
import com.nogeeksbrewing.graphql.r2dbc.BatchRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class GraphQLController {

    private static final Log LOG = LogFactory.getLog(GraphQLController.class);

    private final BatchRepository batchRepository;

    public GraphQLController(@Autowired BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @QueryMapping
    public Flux<Batch> batches(@Argument Long batchDate) {
        long start = System.currentTimeMillis();
        try {
            return batchRepository.findByBatchDateGreaterThanEqual(batchDate);
        } finally {
            long end = System.currentTimeMillis();
            LOG.info("Got batch data in " + (end - start) + "ms");
        }
    }

}
