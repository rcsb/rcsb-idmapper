package org.rcsb.idmapper.frontend.input;

import java.util.List;

public class GroupInput extends Input {
    public List<String> ids;
    public Input.AggregationMethod aggregation_method;
    public int similarity_cutoff;
}
