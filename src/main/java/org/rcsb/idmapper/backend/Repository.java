package org.rcsb.idmapper.backend;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local data repository. Initially just a bunch of in-memory Maps
 *
 * @since 27 Feb 2023
 * @author ingvord
 * @author Yana Rose
 */
public class Repository {

    private final Map<String, String[]> entryToAssembly = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToPubmed = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToBranchedEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToNonPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToPolymerInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToMolecularDefinition = new ConcurrentHashMap<>();

    /**
     * Concatenates two arrays
     *
     * @param array1 first array
     * @param array2 second array
     * @return resulting array contains data from {@param array1} and {@param array2}
     */
    private String[] concatenateArrays(String[] array1, String[] array2) {
        String[] updated = new String[array1.length+array2.length];
        System.arraycopy(array1, 0, updated, 0, array1.length);
        System.arraycopy(array2, 0, updated, array1.length, array2.length);
        return updated;
    }

    private void addNonEmptyValues(Map<String, String[]> map, String key, Collection<String> values) {
        if (values == null || values.size() == 0)
            return;
        String[] existingValues = map.getOrDefault(key, new String[]{});
        String[] updatedValues = concatenateArrays(existingValues, values.toArray(new String[0]));
        map.put(key, updatedValues);
    }

    public void addEntryToAssemblyMapping(String entryId, List<String> assemblyIds) {
        addNonEmptyValues(entryToAssembly, entryId, assemblyIds);
    }

    public void addEntryToPubmedMapping(String entryId, Integer pubmedId) {
        if (pubmedId ==  null) return;
        addNonEmptyValues(entryToPubmed, entryId, Collections.singleton(String.valueOf(pubmedId)));
    }

    public void addEntryToPolymerEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToPolymerEntity, entryId, entityIds);
    }

    public void addEntryToBranchedEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToBranchedEntity, entryId, entityIds);
    }

    public void addEntryToNonPolymerEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToNonPolymerEntity, entryId, entityIds);
    }

    public void addPolymerEntityToPolymerInstance(String entityId, List<String> instanceIds) {
        addNonEmptyValues(polymerEntityToPolymerInstance, entityId, instanceIds);
    }

    public void addPolymerEntityToMonomers(String entityId, List<String> moleculeIds) {
        addNonEmptyValues(polymerEntityToMolecularDefinition, entityId, moleculeIds);
    }

    public void addPolymerEntityToPrd(String entityId, String prdId) {
        if (prdId == null) return;
        addNonEmptyValues(polymerEntityToMolecularDefinition, entityId, Collections.singleton(prdId));
    }
}
