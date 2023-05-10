package org.rcsb.idmapper.backend.data.repository;

import java.util.*;

/**
 * Created on 4/20/23.
 *
 * @author Yana Rose
 */
public class AllRepository extends AnyRepository {

    private final List<String> entryIds = Collections.synchronizedList(new ArrayList<>());
    private final List<String> assemblyIds = Collections.synchronizedList(new ArrayList<>());
    private final List<String> polymerEntityIds = Collections.synchronizedList(new ArrayList<>());
    private final List<String> nonPolymerEntityIds = Collections.synchronizedList(new ArrayList<>());
    private final List<String> polymerInstanceIds = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> compIds = Collections.synchronizedSet(new HashSet<>());

    public void addEntry(String entryId) {
        entryIds.add(entryId);
    }

    public void addAssemblies(String entryId, List<String> assemblyIds) {
        var ids = createAssemblyIdentifiers(entryId, assemblyIds);
        this.assemblyIds.addAll(ids);
    }

    public void addPolymerEntities(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        this.polymerEntityIds.addAll(ids);
    }

    public void addNonPolymerEntities(String entryId, List<String> entityIds) {
        var ids = createEntityIdentifiers(entryId, entityIds);
        this.nonPolymerEntityIds.addAll(ids);
    }

    public void addPolymerInstances(String entryId, List<String> instanceIds) {
        var ids = createInstanceIdentifiers(entryId, instanceIds);
        this.polymerInstanceIds.addAll(ids);
    }

    public void addComponents(List<String> compIds) {
        if (compIds == null) return;
        this.compIds.addAll(compIds);
    }

    public List<String> getEntryIds() {
        return entryIds;
    }

    public List<String> getAssemblyIds() {
        return assemblyIds;
    }

    public List<String> getPolymerEntityIds() {
        return polymerEntityIds;
    }

    public List<String> getNonPolymerEntityIds() {
        return nonPolymerEntityIds;
    }

    public List<String> getPolymerInstanceIds() {
        return polymerInstanceIds;
    }

    public List<String> getCompIds() {
        return new ArrayList<>(compIds);
    }
}
