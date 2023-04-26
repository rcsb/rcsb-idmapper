package org.rcsb.idmapper.backend.data.repository;

import org.apache.commons.lang3.ArrayUtils;
import org.rcsb.common.constants.IdentifierSeparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created on 4/20/23.
 *
 * @author Yana Rose
 */
public class AnyRepository {

    void addValuesToList(List<String> toBeExtended, String[] toBeAdded) {
        toBeExtended.addAll(Arrays.asList(toBeAdded));
    }

    String[] addNewValues(String[] arr1, String[] arr2) {
        return Stream.concat(Stream.of(arr1), Stream.of(arr2))
                .distinct()
                .toArray(String[]::new);
    }

    void addValuesToMap(Map<String, String[]> toBeExtended,
                        String toBeAddedKey, String[] toBeAddedValues) {
        toBeExtended.merge(toBeAddedKey, toBeAddedValues, this::addNewValues);
    }

    void addValuesToMapReverse(Map<String, String[]> toBeExtended,
                               String toBeAddedKey, String[] toBeAddedValues) {
        Stream.of(toBeAddedValues).forEach(v -> toBeExtended
                .merge(v, new String[]{toBeAddedKey}, this::addNewValues));
    }

    String[] createCombinedIdentifiers(String token1, List<String> token2, String sep) {
        return token2.stream().map(id -> String.join(sep, token1, id)).toArray(String[]::new);
    }

    String[] createCompIdentifiers(List<String> compIds) {
        return compIds.toArray(String[]::new);
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
