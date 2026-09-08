package org.rcsb.idmapper.backend.data;

import org.rcsb.idmapper.input.Input;
import org.rcsb.mojave.enumeration.RcsbGroupProvenanceContainerIdentifiersGroupProvenanceId;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized mapper between API aggregation methods and group provenance ids in DW data.
 */
public final class AggregationMethodProvenanceMapper {
    private static final EnumMap<Input.AggregationMethod, String> METHOD_TO_PROVENANCE =
            new EnumMap<>(Input.AggregationMethod.class);
    private static final Map<String, Input.AggregationMethod> PROVENANCE_TO_METHOD = new HashMap<>();

    static {
        register(Input.AggregationMethod.sequence_identity,
                RcsbGroupProvenanceContainerIdentifiersGroupProvenanceId.PROVENANCE_SEQUENCE_IDENTITY.value());
        register(Input.AggregationMethod.matching_uniprot_accession,
                RcsbGroupProvenanceContainerIdentifiersGroupProvenanceId.PROVENANCE_MATCHING_UNIPROT_ACCESSION.value());
        register(Input.AggregationMethod.matching_deposit_group_id,
                RcsbGroupProvenanceContainerIdentifiersGroupProvenanceId.PROVENANCE_MATCHING_DEPOSIT_GROUP_ID.value());
        register(Input.AggregationMethod.matching_chemical_component_id,
                RcsbGroupProvenanceContainerIdentifiersGroupProvenanceId.PROVENANCE_MATCHING_CHEMICAL_COMPONENT_ID.value());
    }

    private AggregationMethodProvenanceMapper() {}

    private static void register(Input.AggregationMethod method, String provenanceId) {
        METHOD_TO_PROVENANCE.put(method, provenanceId);
        PROVENANCE_TO_METHOD.put(provenanceId, method);
    }

    public static String toProvenanceId(Input.AggregationMethod aggregationMethod) {
        String provenanceId = METHOD_TO_PROVENANCE.get(aggregationMethod);
        if (provenanceId == null) {
            throw new IllegalArgumentException("Unsupported aggregation method: " + aggregationMethod);
        }
        return provenanceId;
    }

    public static Input.AggregationMethod toAggregationMethod(String provenanceId) {
        Input.AggregationMethod method = PROVENANCE_TO_METHOD.get(provenanceId);
        if (method == null) {
            throw new IllegalArgumentException("Unsupported provenance id: " + provenanceId);
        }
        return method;
    }
}
