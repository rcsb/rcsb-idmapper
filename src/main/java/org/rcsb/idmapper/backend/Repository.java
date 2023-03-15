package org.rcsb.idmapper.backend;

import org.rcsb.idmapper.utils.AppUtils;

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

    public void addEntryToAssemblyMapping(String entryId, List<String> assemblyIds) {
        AppUtils.addNonEmptyValues(entryToAssembly, entryId, assemblyIds);
    }

    public void addEntryToPubmedMapping(String entryId, Integer pubmedId) {
        if (pubmedId ==  null) return;
        AppUtils.addNonEmptyValues(entryToPubmed, entryId, Collections.singleton(String.valueOf(pubmedId)));
    }

    public void addEntryToPolymerEntity(String entryId, List<String> entityIds) {
        AppUtils.addNonEmptyValues(entryToPolymerEntity, entryId, entityIds);
    }

    public void addEntryToBranchedEntity(String entryId, List<String> entityIds) {
        AppUtils.addNonEmptyValues(entryToBranchedEntity, entryId, entityIds);
    }

    public void addEntryToNonPolymerEntity(String entryId, List<String> entityIds) {
        AppUtils.addNonEmptyValues(entryToNonPolymerEntity, entryId, entityIds);
    }

    public void addPolymerEntityToPolymerInstance(String entityId, List<String> instanceIds) {
        AppUtils.addNonEmptyValues(polymerEntityToPolymerInstance, entityId, instanceIds);
    }

    public void addPolymerEntityToMonomers(String entityId, List<String> moleculeIds) {
        AppUtils.addNonEmptyValues(polymerEntityToMolecularDefinition, entityId, moleculeIds);
    }

    public void addPolymerEntityToPrd(String entityId, String prdId) {
        if (prdId == null) return;
        AppUtils.addNonEmptyValues(polymerEntityToMolecularDefinition, entityId, Collections.singleton(prdId));
    }
}
