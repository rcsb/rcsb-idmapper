package org.rcsb.idmapper.backend;

import org.apache.commons.lang3.ArrayUtils;
import org.rcsb.common.constants.IdentifierSeparator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Local data repository. Initially just a bunch of in-memory Maps
 *
 * @since 27 Feb 2023
 * @author ingvord
 * @author Yana Rose
 */
public class Repository {
    // direct mappings
    private final Map<String, String[]> entryToAssembly = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToPubmed = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToBranchedEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> entryToNonPolymerEntity = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToComps = new ConcurrentHashMap<>();
    private final Map<String, String[]> polymerEntityToUniprot = new ConcurrentHashMap<>();
    private final Map<String, String[]> branchedEntityToInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> branchedEntityToComps = new ConcurrentHashMap<>();
    private final Map<String, String[]> nonPolymerEntityToInstance = new ConcurrentHashMap<>();
    private final Map<String, String[]> nonPolymerEntityToComps = new ConcurrentHashMap<>();
    private final Map<String, String[]> compsToDrugBank = new ConcurrentHashMap<>();
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
    private final Map<String, String[]> drugBankToComps = new ConcurrentHashMap<>();

    private void addNonEmptyValues(Map<String, String[]> direct, Map<String, String[]> reverse,
                                   String key, Collection<String> values) {
        if (values == null || values.size() == 0) return;
        direct.merge(key, values.toArray(String[]::new), ArrayUtils::addAll);
        values.forEach(v -> reverse.merge(v, new String[]{key}, ArrayUtils::addAll));

    }

    private List<String> createCombinedIdentifiers(String idComp1, List<String> idsComp2, String sep) {
        if (idsComp2 == null) return Collections.emptyList();
        return idsComp2.stream().map(id -> String.join(sep, idComp1, id)).collect(Collectors.toList());
    }

    private List<String> createAssemblyIdentifiers(String entryId, List<String> assemblyIds) {
        return createCombinedIdentifiers(entryId, assemblyIds, IdentifierSeparator.ASSEMBLY_SEPARATOR);
    }

    private List<String> createEntityIdentifiers(String entryId, List<String> entityIds) {
        return createCombinedIdentifiers(entryId, entityIds, IdentifierSeparator.ENTITY_SEPARATOR);
    }

    private List<String> createInstanceIdentifiers(String entryId, List<String> instanceIds) {
        return createCombinedIdentifiers(entryId, instanceIds, IdentifierSeparator.ENTITY_INSTANCE_SEPARATOR);
    }

    public void addEntryToAssembly(String entryId, List<String> assemblyIds) {
        addNonEmptyValues(entryToAssembly, assemblyToEntry, entryId, createAssemblyIdentifiers(entryId, assemblyIds));
    }

    public void addEntryToPubmed(String entryId, Integer pubmedId) {
        if (pubmedId ==  null) return;
        addNonEmptyValues(entryToPubmed, pubmedToEntry, entryId, Collections.singleton(String.valueOf(pubmedId)));
    }

    public void addEntryToPolymerEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToPolymerEntity, polymerEntityToEntry, entryId, createEntityIdentifiers(entryId, entityIds));
    }

    public void addEntryToBranchedEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToBranchedEntity, branchedEntityToEntry, entryId, createEntityIdentifiers(entryId, entityIds));
    }

    public void addEntryToNonPolymerEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToNonPolymerEntity, nonPolymerEntityToEntry, entryId, createEntityIdentifiers(entryId, entityIds));
    }

    public void addPolymerEntityToInstance(String entryId, String entityId, List<String> instanceIds) {
        addNonEmptyValues(polymerEntityToInstance, polymerInstanceToEntity, entityId, createInstanceIdentifiers(entryId, instanceIds));
    }

    public void addPolymerEntityToCcd(String entityId, List<String> molIds) {
        addNonEmptyValues(polymerEntityToComps, compsToPolymerEntity, entityId, molIds);
    }

    public void addPolymerEntityToBird(String entityId, String molId) {
        if (molId == null) return;
        addNonEmptyValues(polymerEntityToComps, compsToPolymerEntity, entityId, Collections.singleton(molId));
    }

    public void addBranchedEntityToInstance(String entry, String entityId, List<String> instanceIds) {
        addNonEmptyValues(branchedEntityToInstance, branchedInstanceToEntity, entityId, createInstanceIdentifiers(entry, instanceIds));
    }

    public void addBranchedEntityToCcd(String entityId, List<String> molIds) {
        addNonEmptyValues(branchedEntityToComps, compsToBranchedEntity, entityId, molIds);
    }

    public void addBranchedEntityToBird(String entityId, String molId) {
        if (molId == null) return;
        addNonEmptyValues(branchedEntityToComps, compsToBranchedEntity, entityId, Collections.singleton(molId));
    }

    public void addNonPolymerEntityToInstance(String entry, String entityId, List<String> instanceIds) {
        addNonEmptyValues(nonPolymerEntityToInstance, nonPolymerInstanceToEntity, entityId, createInstanceIdentifiers(entry, instanceIds));
    }

    public void addNonPolymerEntityToComps(String entityId, String molId) {
        if (molId == null) return;
        addNonEmptyValues(nonPolymerEntityToComps, compsToNonPolymerEntity, entityId, Collections.singleton(molId));
    }

    public void addPolymerEntityToUniprot(String entityId, List<String> uniprotIds) {
        addNonEmptyValues(polymerEntityToUniprot, uniprotToPolymerEntity, entityId, uniprotIds);
    }

    public void addChemCompsToDrugBank(String compId, String drugBankId) {
        if (drugBankId == null) return;
        addNonEmptyValues(compsToDrugBank, drugBankToComps, compId, Collections.singleton(drugBankId));
    }
}
