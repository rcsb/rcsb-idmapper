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

    private void addNonEmptyValues(Map<String, String[]> map, String key, Collection<String> values) {
        if (values == null || values.size() == 0)
            return;
        map.merge(key, values.toArray(String[]::new), ArrayUtils::addAll);
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
        addNonEmptyValues(entryToAssembly, entryId, createAssemblyIdentifiers(entryId, assemblyIds));
    }

    public void addEntryToPubmed(String entryId, Integer pubmedId) {
        if (pubmedId ==  null) return;
        addNonEmptyValues(entryToPubmed, entryId, Collections.singleton(String.valueOf(pubmedId)));
    }

    public void addEntryToPolymerEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToPolymerEntity, entryId, createEntityIdentifiers(entryId, entityIds));
    }

    public void addEntryToBranchedEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToBranchedEntity, entryId, createEntityIdentifiers(entryId, entityIds));
    }

    public void addEntryToNonPolymerEntity(String entryId, List<String> entityIds) {
        addNonEmptyValues(entryToNonPolymerEntity, entryId, createEntityIdentifiers(entryId, entityIds));
    }

    public void addPolymerEntityToInstance(String entryId, String entityId, List<String> instanceIds) {
        addNonEmptyValues(polymerEntityToInstance, entityId, createInstanceIdentifiers(entryId, instanceIds));
    }

    public void addPolymerEntityToCcd(String entityId, List<String> molIds) {
        addNonEmptyValues(polymerEntityToComps, entityId, molIds);
    }

    public void addPolymerEntityToBird(String entityId, String molId) {
        if (molId == null) return;
        addNonEmptyValues(polymerEntityToComps, entityId, Collections.singleton(molId));
    }

    public void addBranchedEntityToInstance(String entry, String entityId, List<String> instanceIds) {
        addNonEmptyValues(branchedEntityToInstance, entityId, createInstanceIdentifiers(entry, instanceIds));
    }

    public void addBranchedEntityToCcd(String entityId, List<String> molIds) {
        addNonEmptyValues(branchedEntityToComps, entityId, molIds);
    }

    public void addBranchedEntityToBird(String entityId, String molId) {
        if (molId == null) return;
        addNonEmptyValues(branchedEntityToComps, entityId, Collections.singleton(molId));
    }

    public void addNonPolymerEntityToInstance(String entry, String entityId, List<String> instanceIds) {
        addNonEmptyValues(nonPolymerEntityToInstance, entityId, createInstanceIdentifiers(entry, instanceIds));
    }

    public void addNonPolymerEntityToComps(String entityId, String molId) {
        if (molId == null) return;
        addNonEmptyValues(nonPolymerEntityToComps, entityId, Collections.singleton(molId));
    }

    public void addPolymerEntityToUniprot(String entityId, List<String> uniprotIds) {
        addNonEmptyValues(polymerEntityToUniprot, entityId, uniprotIds);
    }

    public void addChemCompsToDrugBank(String compId, String drugBankId) {
        if (drugBankId == null) return;
        addNonEmptyValues(compsToDrugBank, compId, Collections.singleton(drugBankId));
    }
}
