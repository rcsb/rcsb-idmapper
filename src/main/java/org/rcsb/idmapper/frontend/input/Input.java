package org.rcsb.idmapper.frontend.input;

/**
 * Represents input provided from downstream client e.g. Arches
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public abstract class Input {

    public enum Type {
        entry,
        polymer_entity,
        non_polymer_entity,
        polymer_instance,
        assembly,
        mol_definition
    }

    public enum ContentType {
        experimental,
        computational
    }

    public enum AggregationMethod {
        matching_deposit_group_id,
        sequence_identity,
        matching_uniprot_accession
    }
}


