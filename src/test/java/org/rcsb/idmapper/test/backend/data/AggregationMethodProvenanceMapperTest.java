package org.rcsb.idmapper.test.backend.data;

import org.junit.jupiter.api.Test;
import org.rcsb.idmapper.backend.data.AggregationMethodProvenanceMapper;
import org.rcsb.idmapper.input.Input;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AggregationMethodProvenanceMapperTest {
    @Test
    public void methodToProvenanceToMethod_MustRoundTrip() {
        for (Input.AggregationMethod method : Input.AggregationMethod.values()) {
            String provenanceId = AggregationMethodProvenanceMapper.toProvenanceId(method);
            assertEquals(method, AggregationMethodProvenanceMapper.toAggregationMethod(provenanceId));
        }
    }

    @Test
    public void methodToProvenance_MustBeUnique() {
        Set<String> provenanceIds = Arrays.stream(Input.AggregationMethod.values())
                .map(AggregationMethodProvenanceMapper::toProvenanceId)
                .collect(Collectors.toSet());
        assertEquals(Input.AggregationMethod.values().length, provenanceIds.size());
    }

    @Test
    public void unknownProvenance_MustThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> AggregationMethodProvenanceMapper.toAggregationMethod("unknown_provenance"));
    }
}
