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
    public static final int EXPECTED_KEYS = 1_500_000;
    public static final int EXPECTED_VALUES_PER_KEY = 12;


    void addValuesToDirectMap(Multimap<String, String> toBeExtended,
                              String toBeAddedKey, List<String> toBeAddedValues) {
        toBeAddedValues.forEach(v -> toBeExtended.put(toBeAddedKey, v));
    }

    void addValuesToReverseMap(Multimap<String, String> toBeExtended,
                              String toBeAddedKey, List<String> toBeAddedValues) {
        toBeAddedValues.forEach(v -> toBeExtended.put(v, toBeAddedKey));
    }

    String createAssemblyIdentifier(String entryId, String assemblyId) {
        return String.join(IdentifierSeparator.ASSEMBLY_SEPARATOR, entryId, assemblyId);
    }

    List<String> createAssemblyIdentifiers(String entryId, List<String> assemblyIds) {
        return assemblyIds.stream().map(id -> createAssemblyIdentifier(entryId, id)).toList();
    }

    String createEntityIdentifier(String entryId, String entityId) {
        return String.join(IdentifierSeparator.ENTITY_SEPARATOR, entryId, entityId);
    }

    List<String> createEntityIdentifiers(String entryId, List<String> entityIds) {
        return entityIds.stream().map(id -> createEntityIdentifier(entryId, id)).toList();
    }

    String createInstanceIdentifier(String entryId, String instanceId) {
        return String.join(IdentifierSeparator.ENTITY_INSTANCE_SEPARATOR, entryId, instanceId);
    }

    List<String> createInstanceIdentifiers(String entryId, List<String> instanceIds) {
        return instanceIds.stream().map(id -> createInstanceIdentifier(entryId, id)).toList();
    }
}
