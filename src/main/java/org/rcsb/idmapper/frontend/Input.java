package org.rcsb.idmapper.frontend;

/**
 * Represents input provided from downstream client e.g. Arches
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public abstract class Input {

    public static enum Type{
        entry,
        polymer_entity,
        non_polymer_entity,
        polymer_instance,
        assembly,
        mol_definition
    }

    public static enum ContentType{
        experimental,
        computational
    }

    public static enum AggregationMethod {
        matching_deposit_group_id,
        sequence_identity,
        matching_uniprot_accession
    }
}


