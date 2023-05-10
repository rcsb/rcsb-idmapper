package org.rcsb.idmapper.backend.data.repository;

import com.google.common.collect.Multimap;
import org.rcsb.common.constants.IdentifierSeparator;

import java.util.List;

/**
 * Created on 4/20/23.
 *
 * @author Yana Rose
 */
public class AnyRepository {

    void addValuesToDirectMap(Multimap<String, String> toBeExtended,
                              String toBeAddedKey, List<String> toBeAddedValues) {
        toBeAddedValues.forEach(v -> toBeExtended.put(toBeAddedKey, v));
    }

    void addValuesToReverseMap(Multimap<String, String> toBeExtended,
                              String toBeAddedKey, List<String> toBeAddedValues) {
        toBeAddedValues.forEach(v -> toBeExtended.put(v, toBeAddedKey));
    }

    List<String> createCombinedIdentifiers(String token1, List<String> token2, String sep) {
        return token2.stream().map(id -> String.join(sep, token1, id)).toList();
    }

    List<String> createAssemblyIdentifiers(String entryId, List<String> assemblyIds) {
        return createCombinedIdentifiers(entryId, assemblyIds, IdentifierSeparator.ASSEMBLY_SEPARATOR);
    }

    List<String> createEntityIdentifiers(String entryId, List<String> entityIds) {
        return createCombinedIdentifiers(entryId, entityIds, IdentifierSeparator.ENTITY_SEPARATOR);
    }

    List<String> createInstanceIdentifiers(String entryId, List<String> instanceIds) {
        return createCombinedIdentifiers(entryId, instanceIds, IdentifierSeparator.ENTITY_INSTANCE_SEPARATOR);
    }
}
