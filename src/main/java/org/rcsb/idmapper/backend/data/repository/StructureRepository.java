package org.rcsb.idmapper.backend.data.repository;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local data repository. Initially just a bunch of in-memory Maps
 *
 * @since 27 Feb 2023
 * @author ingvord
 * @author Yana Rose
 */
public class StructureRepository extends AnyRepository {
    // direct mappings
    private final Map<String, String[]> entryToAssembly = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToPubmed = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToBranchedEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToNonPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToComps = new ConcurrentHashMap<>();

    private final Map<String, String[]> polymerEntityToInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToComps = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToUniprot = new ConcurrentHashMap<>();
    private final Map<String, String[]> branchedEntityToInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> branchedEntityToComps = new ConcurrentHashMap<>();
    private final Map<String, String[]> nonPolymerEntityToInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> nonPolymerEntityToComps = new ConcurrentHashMap<>();
    // reverse mappings
    private final Map<String, String[]> assemblyToEntry = new ConcurrentHashMap<>();
    private final Map<String, String[]> pubmedToEntry = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToEntry = new ConcurrentHashMap<>();
    private final Map<String, String[]> branchedEntityToEntry = new ConcurrentHashMap<>();
    private final Map<String, String[]> nonPolymerEntityToEntry = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerInstanceToEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> compsToPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> uniprotToPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> branchedInstanceToEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> compsToBranchedEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> nonPolymerInstanceToEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> compsToNonPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> compsToEntry = new ConcurrentHashMap<>();

    public void addEntryToAssembly(String entryId, List<String> assemblyIds) {
        var ids = createAssemblyIdentifiers(entryId, assemblyIds);
        addNonEmptyValues(entryToAssembly, entryId, ids);
        addNonEmptyValuesReverse(assemblyToEntry, entryId, ids);
    }

    public void addEntryToPubmed(String entryId, Integer pubmedId) {
        if (pubmedId ==  null) return;
        var ids = new String[]{String.valueOf(pubmedId)};
        addNonEmptyValues(entryToPubmed, entryId, ids);
        addNonEmptyValuesReverse(pubmedToEntry, entryId, ids);
    }

    public void addEntryToPolymerEntity(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        addNonEmptyValues(entryToPolymerEntity, entryId, ids);
        addNonEmptyValuesReverse(polymerEntityToEntry, entryId, ids);
    }

    public void addEntryToBranchedEntity(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        addNonEmptyValues(entryToBranchedEntity, entryId, ids);
        addNonEmptyValuesReverse(branchedEntityToEntry, entryId, ids);
    }

    public void addEntryToNonPolymerEntity(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        addNonEmptyValues(entryToNonPolymerEntity, entryId, ids);
        addNonEmptyValuesReverse(nonPolymerEntityToEntry, entryId, ids);
    }

    public void addEntryToComps(String entryId, List<String> compIds) {
        var ids = createCompIdentifiers(compIds);
        addNonEmptyValues(entryToComps, entryId, ids);
        addNonEmptyValuesReverse(compsToEntry, entryId, ids);
    }

    public void addPolymerEntityToInstance(String entryId, String entityId, List<String> instanceIds) {
        var ids = createInstanceIdentifiers(entryId, instanceIds);
        addNonEmptyValues(polymerEntityToInstance, entityId, ids);
        addNonEmptyValuesReverse(polymerInstanceToEntity, entityId, ids);
    }

    public void addPolymerEntityToCcd(String entityId, List<String> compIds) {
        var ids = createCompIdentifiers(compIds);
        addNonEmptyValues(polymerEntityToComps, entityId, ids);
        addNonEmptyValuesReverse(compsToPolymerEntity, entityId, ids);
    }

    public void addPolymerEntityToBird(String entityId, String compId) {
        if (compId == null) return;
        var ids = new String[]{compId};
        addNonEmptyValues(polymerEntityToComps, entityId, ids);
        addNonEmptyValuesReverse(compsToPolymerEntity, entityId, ids);
    }

    public void addBranchedEntityToInstance(String entry, String entityId, List<String> instanceIds) {
        var ids = createInstanceIdentifiers(entry, instanceIds);
        addNonEmptyValues(branchedEntityToInstance, entityId, ids);
        addNonEmptyValuesReverse(branchedInstanceToEntity, entityId, ids);
    }

    public void addBranchedEntityToCcd(String entityId, List<String> compIds) {
        var ids = createCompIdentifiers(compIds);
        addNonEmptyValues(branchedEntityToComps, entityId, ids);
        addNonEmptyValuesReverse(compsToBranchedEntity, entityId, ids);
    }

    public void addBranchedEntityToBird(String entityId, String compId) {
        if (compId == null) return;
        var ids = new String[]{compId};
        addNonEmptyValues(branchedEntityToComps, entityId, ids);
        addNonEmptyValuesReverse(compsToBranchedEntity, entityId, ids);
    }

    public void addNonPolymerEntityToInstance(String entry, String entityId, List<String> instanceIds) {
        var ids = createInstanceIdentifiers(entry, instanceIds);
        addNonEmptyValues(nonPolymerEntityToInstance, entityId, ids);
        addNonEmptyValuesReverse(nonPolymerInstanceToEntity, entityId, ids);
    }

    public void addNonPolymerEntityToComps(String entityId, String compId) {
        if (compId == null) return;
        var ids = new String[]{compId};
        addNonEmptyValues(nonPolymerEntityToComps, entityId, ids);
        addNonEmptyValuesReverse(compsToNonPolymerEntity, entityId, ids);
    }

    public void addPolymerEntityToUniprot(String entityId, List<String> uniprotIds) {
        addNonEmptyValues(polymerEntityToUniprot, entityId, uniprotIds.toArray(String[]::new));
        addNonEmptyValuesReverse(uniprotToPolymerEntity, entityId, uniprotIds.toArray(String[]::new));
    }

    public Map<String, String[]> getEntryToAssembly() {
        return entryToAssembly;
    }

    public Map<String, String[]> getEntryToPubmed() {
        return entryToPubmed;
    }

    public Map<String, String[]> getEntryToPolymerEntity() {
        return entryToPolymerEntity;
    }

    public Map<String, String[]> getEntryToBranchedEntity() {
        return entryToBranchedEntity;
    }

    public Map<String, String[]> getEntryToNonPolymerEntity() {
        return entryToNonPolymerEntity;
    }

    public Map<String, String[]> getPolymerEntityToInstance() {
        return polymerEntityToInstance;
    }

    public Map<String, String[]> getPolymerEntityToComps() {
        return polymerEntityToComps;
    }

    public Map<String, String[]> getPolymerEntityToUniprot() {
        return polymerEntityToUniprot;
    }

    public Map<String, String[]> getBranchedEntityToInstance() {
        return branchedEntityToInstance;
    }

    public Map<String, String[]> getBranchedEntityToComps() {
        return branchedEntityToComps;
    }

    public Map<String, String[]> getNonPolymerEntityToInstance() {
        return nonPolymerEntityToInstance;
    }

    public Map<String, String[]> getNonPolymerEntityToComps() {
        return nonPolymerEntityToComps;
    }

    public Map<String, String[]> getAssemblyToEntry() {
        return assemblyToEntry;
    }

    public Map<String, String[]> getPubmedToEntry() {
        return pubmedToEntry;
    }

    public Map<String, String[]> getPolymerEntityToEntry() {
        return polymerEntityToEntry;
    }

    public Map<String, String[]> getBranchedEntityToEntry() {
        return branchedEntityToEntry;
    }

    public Map<String, String[]> getNonPolymerEntityToEntry() {
        return nonPolymerEntityToEntry;
    }

    public Map<String, String[]> getPolymerInstanceToEntity() {
        return polymerInstanceToEntity;
    }

    public Map<String, String[]> getCompsToPolymerEntity() {
        return compsToPolymerEntity;
    }

    public Map<String, String[]> getUniprotToPolymerEntity() {
        return uniprotToPolymerEntity;
    }

    public Map<String, String[]> getBranchedInstanceToEntity() {
        return branchedInstanceToEntity;
    }

    public Map<String, String[]> getCompsToBranchedEntity() {
        return compsToBranchedEntity;
    }

    public Map<String, String[]> getNonPolymerInstanceToEntity() {
        return nonPolymerInstanceToEntity;
    }

    public Map<String, String[]> getCompsToNonPolymerEntity() {
        return compsToNonPolymerEntity;
    }

    public Map<String, String[]> getEntryToComps() {
        return entryToComps;
    }

    public Map<String, String[]> getCompsToEntry() {
        return compsToEntry;
    }
}
