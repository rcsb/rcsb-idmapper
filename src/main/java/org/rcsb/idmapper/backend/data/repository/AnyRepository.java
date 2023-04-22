package org.rcsb.idmapper.backend.data.repository;

import org.apache.commons.lang3.ArrayUtils;
import org.rcsb.common.constants.IdentifierSeparator;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created on 4/20/23.
 *
 * @author Yana Rose
 */
public class AnyRepository {

    void addNonEmptyValues(Collection<String> toBeExtended, String[] toBeAdded) {
        if (toBeAdded == null || toBeAdded.length == 0) return;
        toBeExtended.addAll(Arrays.asList(toBeAdded));
    }

    void addNonEmptyValues(Map<String, String[]> toBeExtended,
                                   String toBeAddedKey, String[] toBeAddedValues) {
        if (toBeAddedValues == null || toBeAddedValues.length == 0) return;
        toBeExtended.merge(toBeAddedKey, toBeAddedValues, ArrayUtils::addAll);
    }

    void addNonEmptyValuesReverse(Map<String, String[]> toBeExtended,
                                  String toBeAddedKey, String[] toBeAddedValues) {
        if (toBeAddedValues == null || toBeAddedValues.length == 0) return;
        Stream.of(toBeAddedValues).forEach(v -> toBeExtended.merge(v, new String[]{toBeAddedKey}, ArrayUtils::addAll));
    }

    String[] createCombinedIdentifiers(String token1, List<String> token2, String sep) {
        if (token2 == null) return new String[0];
        return token2.stream().map(id -> String.join(sep, token1, id)).toArray(String[]::new);
    }

    String[] createAssemblyIdentifiers(String entryId, List<String> assemblyIds) {
        return createCombinedIdentifiers(entryId, assemblyIds, IdentifierSeparator.ASSEMBLY_SEPARATOR);
    }

    String[] createEntityIdentifiers(String entryId, List<String> entityIds) {
        return createCombinedIdentifiers(entryId, entityIds, IdentifierSeparator.ENTITY_SEPARATOR);
    }

    String[] createInstanceIdentifiers(String entryId, List<String> instanceIds) {
        return createCombinedIdentifiers(entryId, instanceIds, IdentifierSeparator.ENTITY_INSTANCE_SEPARATOR);
    }
}
