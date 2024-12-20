package com.nogeeksbrewing.graphql.json;

import org.springframework.data.annotation.Id;

public record Batch(@Id Long batchId,
                    String brewfatherId,
                    String batchName,
                    Long batchNumber,
                    Long batchDate,
                    String batchStatus,
                    String batchStyle) { }
