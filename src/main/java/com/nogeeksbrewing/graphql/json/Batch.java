package com.nogeeksbrewing.graphql.json;

import org.springframework.data.annotation.Id;

public record Batch(@Id Long id,
                    String brewfatherId,
                    String name,
                    Long batchNumber,
                    Long batchDate,
                    String status,
                    String style) { }
