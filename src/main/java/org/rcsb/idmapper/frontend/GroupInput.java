package org.rcsb.idmapper.frontend;

import com.fasterxml.jackson.annotation.JsonInclude;

public class GroupInput extends Input {
    public String id;
    public Input.AggregationMethod aggregation_method;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public int similarity_cutoff;
}
