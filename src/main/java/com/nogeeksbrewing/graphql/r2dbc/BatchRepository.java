package com.nogeeksbrewing.graphql.r2dbc;


import com.nogeeksbrewing.graphql.json.Batch;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface BatchRepository extends R2dbcRepository<Batch, String> {

    Mono<Batch> findByBrewfatherId(String brewfatherId);

}
