package org.rcsb.idmapper.backend.data.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<String> compIds = Collections.synchronizedList(new ArrayList<>());

    public void addEntry(String entryId) {
        entryIds.add(entryId);
    }

    public void addAssemblies(String entryId, List<String> assemblyIds) {
        String[] ids = createAssemblyIdentifiers(entryId, assemblyIds);
        addNonEmptyValues(this.assemblyIds, ids);
    }

    public void addPolymerEntities(String entryId, List<String> entityIds) {
        String[] ids = createEntityIdentifiers(entryId, entityIds);
        addNonEmptyValues(polymerEntityIds, ids);
    }

    public void addNonPolymerEntities(String entryId, List<String> entityIds) {
        String[] ids = createEntityIdentifiers(entryId, entityIds);
        addNonEmptyValues(nonPolymerEntityIds, ids);
    }

    public void addPolymerInstances(String entryId, List<String> instanceIds) {
        String[] ids = createInstanceIdentifiers(entryId, instanceIds);
        addNonEmptyValues(polymerInstanceIds, ids);
    }

    public void addComponents(List<String> compIds) {
        if (compIds == null) return;
        compIds.forEach(c -> {if (!this.compIds.contains(c)) this.compIds.add(c);});
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
        return compIds;
    }
}
