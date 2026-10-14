package org.rcsb.idmapper.backend;

import org.rcsb.common.constants.IdentifierRegex;
import org.rcsb.common.constants.IdentifierSeparator;
import org.rcsb.common.constants.PdbIdFlavor;
import org.rcsb.idmapper.input.Input;

import java.util.Locale;

/**
 * Utilities for parsing backend DB flavor configuration and normalizing input IDs
 * to the flavor expected by backend data.
 */
public final class IdFlavorSupport {
    private IdFlavorSupport() {}

    public static PdbIdFlavor parseConfiguredFlavor(String value) {
        if (value == null || value.isBlank()) {
            return PdbIdFlavor.LEGACY;
        }
        try {
            return PdbIdFlavor.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for env var DB_FLAVOR: " + value +
                    ". Allowed values: legacy, extended", e);
        }
    }

    public static String normalizeTranslateInputId(String inputId, Input.Type from, PdbIdFlavor backendFlavor) {
        String trimmed = trimOrThrow(inputId);
        return switch (from) {
            case entry -> normalizeEntryIdForBackend(trimmed, backendFlavor);
            case assembly -> normalizeEntryScopedId(trimmed, IdentifierSeparator.ASSEMBLY_SEPARATOR, backendFlavor);
            case polymer_entity, branched_entity, non_polymer_entity ->
                    normalizeEntryScopedId(trimmed, IdentifierSeparator.ENTITY_SEPARATOR, backendFlavor);
            case polymer_instance, branched_instance, non_polymer_instance ->
                    normalizeEntryScopedId(trimmed, IdentifierSeparator.ENTITY_INSTANCE_SEPARATOR, backendFlavor);
            default -> trimmed;
        };
    }

    public static String normalizeGroupInputId(String inputId, Input.AggregationMethod method, PdbIdFlavor backendFlavor) {
        String trimmed = trimOrThrow(inputId);
        return switch (method) {
            case matching_deposit_group_id -> normalizeTranslateInputId(trimmed, Input.Type.entry, backendFlavor);
            case sequence_identity, matching_uniprot_accession ->
                    normalizeTranslateInputId(trimmed, Input.Type.polymer_entity, backendFlavor);
            // ignored for now as requested
            case matching_chemical_component_id -> trimmed;
        };
    }

    static String normalizeEntryIdForBackend(String entryId, PdbIdFlavor backendFlavor) {
        String normalizedInput = trimOrThrow(entryId);

        boolean isPdbId = IdentifierRegex.isAnyPdbId(normalizedInput)
                || IdentifierRegex.isAnyPdbId(normalizedInput.toLowerCase(Locale.ROOT));

        // CSM/non-PDB IDs are passed through unchanged.
        if (!isPdbId) {
            return normalizedInput;
        }

        if (backendFlavor == PdbIdFlavor.LEGACY) {
            return IdentifierRegex.normalizePdbId(normalizedInput, PdbIdFlavor.LEGACY);
        } else if (backendFlavor == PdbIdFlavor.EXTENDED) {
            String lowerCaseInput = normalizedInput.toLowerCase(Locale.ROOT);
            if (!IdentifierRegex.isExtPdbId(lowerCaseInput)) {
                throw new IllegalArgumentException("Invalid PDB ID for db.flavor=extended: " + entryId +
                        ". Expected extended format, e.g. pdb_00001abc");
            }
            return IdentifierRegex.normalizePdbId(lowerCaseInput, PdbIdFlavor.EXTENDED);
        } else {
            throw new IllegalStateException("Unsupported backend PDB ID flavor: " + backendFlavor);
        }
    }

    private static String normalizeEntryScopedId(String id, String separator, PdbIdFlavor backendFlavor) {
        int separatorPos = id.lastIndexOf(separator);
        if (separatorPos <= 0) {
            // preserve current permissive behavior for malformed IDs
            return id;
        }
        String entryPart = id.substring(0, separatorPos);
        String suffixPart = id.substring(separatorPos);
        return normalizeEntryIdForBackend(entryPart, backendFlavor) + suffixPart;
    }

    private static String trimOrThrow(String inputId) {
        if (inputId == null) {
            throw new IllegalArgumentException("Invalid ID: null");
        }
        return inputId.trim();
    }
}
