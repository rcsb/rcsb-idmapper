package org.rcsb.idmapper.backend.data.repository;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;

/**
 * Local data repository. Initially just a bunch of in-memory Maps
 *
 * @since 27 Feb 2023
 * @author ingvord
 * @author Yana Rose
 */
public class StructureRepository extends AnyRepository {
    // direct mappings
    private final Multimap<String, String> entryToAssembly = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> entryToPubmed = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> entryToPolymerEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> entryToBranchedEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> entryToNonPolymerEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> entryToComps = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> polymerEntityToInstance = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> polymerEntityToComps = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> polymerEntityToUniprot = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> branchedEntityToInstance = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> branchedEntityToComps = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> nonPolymerEntityToInstance = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    private final Multimap<String, String> nonPolymerEntityToComps = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    // reverse mappings
    Multimap<String, String> assemblyToEntry = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> pubmedToEntry = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> polymerEntityToEntry = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> branchedEntityToEntry = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> nonPolymerEntityToEntry = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> polymerInstanceToEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> compsToPolymerEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> uniprotToPolymerEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> branchedInstanceToEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> compsToBranchedEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> nonPolymerInstanceToEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> compsToNonPolymerEntity = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);
    Multimap<String, String> compsToEntry = HashMultimap.create(EXPECTED_KEYS, EXPECTED_VALUES_PER_KEY);

    public void addEntryToAssembly(String entryId, List<String> assemblyIds) {
        var ids = createAssemblyIdentifiers(entryId, assemblyIds);
        addValuesToDirectMap(entryToAssembly, entryId, ids);
        addValuesToReverseMap(assemblyToEntry, entryId, ids);
    }

    public void addEntryToPubmed(String entryId, Integer pubmedId) {
        if (pubmedId ==  null) return;
        var ids = List.of(String.valueOf(pubmedId));
        addValuesToDirectMap(entryToPubmed, entryId, ids);
        addValuesToReverseMap(pubmedToEntry, entryId, ids);
    }

    public void addEntryToPolymerEntity(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        addValuesToDirectMap(entryToPolymerEntity, entryId, ids);
        addValuesToReverseMap(polymerEntityToEntry, entryId, ids);
    }

    public void addEntryToBranchedEntity(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        addValuesToDirectMap(entryToBranchedEntity, entryId, ids);
        addValuesToReverseMap(branchedEntityToEntry, entryId, ids);
    }

    public void addEntryToNonPolymerEntity(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        addValuesToDirectMap(entryToNonPolymerEntity, entryId, ids);
        addValuesToReverseMap(nonPolymerEntityToEntry, entryId, ids);
    }

    public void addEntryToComps(String entryId, List<String> ids) {
        addValuesToDirectMap(entryToComps, entryId, ids);
        addValuesToReverseMap(compsToEntry, entryId, ids);
    }

    public void addPolymerEntityToInstance(String entryId, String entityId, List<String> instanceIds) {
        var id = createEntityIdentifier(entryId, entityId);
        var ids = createInstanceIdentifiers(entryId, instanceIds);
        addValuesToDirectMap(polymerEntityToInstance, id, ids);
        addValuesToReverseMap(polymerInstanceToEntity, id, ids);
    }

    public void addPolymerEntityToCcd(String entryId, String entityId, List<String> ids) {
        var id = createEntityIdentifier(entryId, entityId);
        addValuesToDirectMap(polymerEntityToComps, id, ids);
        addValuesToReverseMap(compsToPolymerEntity, id, ids);
    }

    public void addPolymerEntityToBird(String entryId, String entityId, String prdId) {
        if (prdId == null) return;
        var id = createEntityIdentifier(entryId, entityId);
        var ids = List.of(prdId);
        addValuesToDirectMap(polymerEntityToComps, id, ids);
        addValuesToReverseMap(compsToPolymerEntity, id, ids);
    }

    public void addBranchedEntityToInstance(String entryId, String entityId, List<String> instanceIds) {
        var id = createEntityIdentifier(entryId, entityId);
        var ids = createInstanceIdentifiers(entryId, instanceIds);
        addValuesToDirectMap(branchedEntityToInstance, id, ids);
        addValuesToReverseMap(branchedInstanceToEntity, id, ids);
    }

    public void addBranchedEntityToCcd(String entryId, String entityId, List<String> ids) {
        var id = createEntityIdentifier(entryId, entityId);
        addValuesToDirectMap(branchedEntityToComps, id, ids);
        addValuesToReverseMap(compsToBranchedEntity, id, ids);
    }

    public void addBranchedEntityToBird(String entryId, String entityId, String prdId) {
        if (prdId == null) return;
        var id = createEntityIdentifier(entryId, entityId);
        var ids = List.of(prdId);
        addValuesToDirectMap(branchedEntityToComps, id, ids);
        addValuesToReverseMap(compsToBranchedEntity, id, ids);
    }

    public void addNonPolymerEntityToInstance(String entryId, String entityId, List<String> instanceIds) {
        var id = createEntityIdentifier(entryId, entityId);
        var ids = createInstanceIdentifiers(entryId, instanceIds);
        addValuesToDirectMap(nonPolymerEntityToInstance, id, ids);
        addValuesToReverseMap(nonPolymerInstanceToEntity, id, ids);
    }

    public void addNonPolymerEntityToComps(String entryId, String entityId, String compId) {
        if (compId == null) return;
        var id = createEntityIdentifier(entryId, entityId);
        var ids = List.of(compId);
        addValuesToDirectMap(nonPolymerEntityToComps, id, ids);
        addValuesToReverseMap(compsToNonPolymerEntity, id, ids);
    }

    public void addPolymerEntityToUniprot(String entryId, String entityId, List<String> ids) {
        var id = createEntityIdentifier(entryId, entityId);
        addValuesToDirectMap(polymerEntityToUniprot, id, ids);
        addValuesToReverseMap(uniprotToPolymerEntity, id, ids);
    }

    public Collection<String> getEntryToAssembly(String entryId) {
        return entryToAssembly.get(entryId);

    }

    public Collection<String> getEntryToPubmed(String pubmedId) {
        return entryToPubmed.get(pubmedId);
    }

    public Collection<String> getEntryToPolymerEntity(String entryId) {
        return entryToPolymerEntity.get(entryId);
    }

    public Collection<String> getEntryToBranchedEntity(String entryId) {
        return entryToBranchedEntity.get(entryId);
    }

    public Collection<String> getEntryToNonPolymerEntity(String entryId) {
        return entryToNonPolymerEntity.get(entryId);
    }

    public Collection<String> getPolymerEntityToInstance(String entityId) {
        return polymerEntityToInstance.get(entityId);
    }

    public Collection<String> getPolymerEntityToComps(String entityId) {
        return polymerEntityToComps.get(entityId);
    }

    public Collection<String> getPolymerEntityToUniprot(String entityId) {
        return polymerEntityToUniprot.get(entityId);
    }

    public Collection<String> getBranchedEntityToInstance(String entityId) {
        return branchedEntityToInstance.get(entityId);
    }

    public Collection<String> getBranchedEntityToComps(String entityId) {
        return branchedEntityToComps.get(entityId);
    }

    public Collection<String> getNonPolymerEntityToInstance(String entityId) {
        return nonPolymerEntityToInstance.get(entityId);
    }

    public Collection<String> getNonPolymerEntityToComps(String entityId) {
        return nonPolymerEntityToComps.get(entityId);
    }

    public Collection<String> getAssemblyToEntry(String assemblyId) {
        return assemblyToEntry.get(assemblyId);
    }

    public Collection<String> getPubmedToEntry(String pubmedId) {
        return pubmedToEntry.get(pubmedId);
    }

    public Collection<String> getPolymerEntityToEntry(String entityId) {
        return polymerEntityToEntry.get(entityId);
    }

    public Collection<String> getBranchedEntityToEntry(String entityId) {
        return branchedEntityToEntry.get(entityId);
    }

    public Collection<String> getNonPolymerEntityToEntry(String entityId) {
        return nonPolymerEntityToEntry.get(entityId);
    }

    public Collection<String> getPolymerInstanceToEntity(String instanceId) {
        return polymerInstanceToEntity.get(instanceId);
    }

    public Collection<String> getCompsToPolymerEntity(String compId) {
        return compsToPolymerEntity.get(compId);
    }

    public Collection<String> getUniprotToPolymerEntity(String uniprotId) {
        return uniprotToPolymerEntity.get(uniprotId);
    }

    public Collection<String> getBranchedInstanceToEntity(String instanceId) {
        return branchedInstanceToEntity.get(instanceId);
    }

    public Collection<String> getCompsToBranchedEntity(String compId) {
        return compsToBranchedEntity.get(compId);
    }

    public Collection<String> getNonPolymerInstanceToEntity(String instanceId) {
        return nonPolymerInstanceToEntity.get(instanceId);
    }

    public Collection<String> getCompsToNonPolymerEntity(String compId) {
        return compsToNonPolymerEntity.get(compId);
    }

    public Collection<String> getEntryToComps(String entryId) {
        return entryToComps.get(entryId);
    }

    public Collection<String> getCompsToEntry(String compId) {
        return compsToEntry.get(compId);
    }
}
