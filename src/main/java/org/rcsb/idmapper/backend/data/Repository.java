package org.rcsb.idmapper.backend.data;

import org.rcsb.common.constants.ContentType;
import org.rcsb.common.constants.MongoCollections;
import org.rcsb.idmapper.backend.data.repository.AllRepository;
import org.rcsb.idmapper.backend.data.repository.ComponentRepository;
import org.rcsb.idmapper.backend.data.repository.GroupRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.idmapper.input.Input;

import java.util.*;

/**
 * Gateway to concrete implementations of repositories
 * <p>
 * Created on 4/19/23.
 *
 * @author Yana Rose
 */
public class Repository {

    private final Map<String, Long> documentCounts = new HashMap<>();

    // content type specific repositories
    private final AllRepository allExperimental = new AllRepository();
    private final AllRepository allComputational = new AllRepository();
    private final StructureRepository structureExperimental = new StructureRepository();
    private final StructureRepository structureComputational = new StructureRepository();
    // content type agnostic repositories
    private final GroupRepository group = new GroupRepository();
    private final ComponentRepository component = new ComponentRepository();

    public AllRepository getAllRepository(ContentType ct) {
        return ct == ContentType.experimental
                ? allExperimental
                : allComputational;
    }

    public StructureRepository getStructureRepository(ContentType ct) {
        return ct == ContentType.experimental
                ? structureExperimental
                : structureComputational;
    }

    public GroupRepository getGroupRepository() {
        return group;
    }

    public ComponentRepository getComponentRepository() {
        return component;
    }

    public void addCount(String collectionName, Long count) {
        documentCounts.put(collectionName, count);
    }

    private Collection<String> transit(Collection<String> ids, Input.Type from, Input.Type to, ContentType ct) {
         return ids.stream()
                 .flatMap(id -> lookup(id, from, to, ct).stream())
                 .distinct()
                 .toList();
    }

    public Collection<String> lookup(String id, Input.Type from, Input.Type to, ContentType ct) {
        switch (from) {
            case entry -> {
                switch (to) {
                    case assembly -> {
                        return getStructureRepository(ct).getEntryToAssembly(id);
                    }
                    case polymer_entity -> {
                        return getStructureRepository(ct).getEntryToPolymerEntity(id);
                    }
                    case branched_entity -> {
                        return getStructureRepository(ct).getEntryToBranchedEntity(id);
                    }
                    case non_polymer_entity -> {
                        return getStructureRepository(ct).getEntryToNonPolymerEntity(id);
                    }
                    case polymer_instance -> {
                        var entityIds = getStructureRepository(ct).getEntryToPolymerEntity(id);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.polymer_instance, ct);
                    }
                    case branched_instance -> {
                        var entityIds = getStructureRepository(ct).getEntryToBranchedEntity(id);
                        return transit(entityIds, Input.Type.branched_entity, Input.Type.branched_instance, ct);
                    }
                    case non_polymer_instance -> {
                        var entityIds = getStructureRepository(ct).getEntryToNonPolymerEntity(id);
                        return transit(entityIds, Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ct);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getEntryToComps(id);
                    }
                    case drug_bank -> {
                        var compIds = getStructureRepository(ct).getEntryToComps(id);
                        return transit(compIds, Input.Type.mol_definition, Input.Type.drug_bank, ct);
                    }
                    case pubmed -> {
                        return getStructureRepository(ct).getEntryToPubmed(id);
                    }
                    case uniprot -> {
                        var entityIds = getStructureRepository(ct).getEntryToPolymerEntity(id);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.uniprot, ct);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case assembly -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getAssemblyToEntry(id);
                    }
                    case polymer_entity -> {
                        var entryIds = getStructureRepository(ct).getAssemblyToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.polymer_entity, ct);
                    }
                    case non_polymer_entity -> {
                        var entryIds = getStructureRepository(ct).getAssemblyToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.non_polymer_entity, ct);
                    }
                    case polymer_instance -> {
                        var entryIds = getStructureRepository(ct).getAssemblyToEntry(id);
                        var entityIds = transit(entryIds, Input.Type.entry, Input.Type.polymer_entity, ct);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.polymer_instance, ct);
                    }
                    case mol_definition -> {
                        var entryIds = getStructureRepository(ct).getAssemblyToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.mol_definition, ct);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case polymer_entity -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getPolymerEntityToEntry(id);
                    }
                    case branched_entity -> {
                        var entryIds = getStructureRepository(ct).getPolymerEntityToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.branched_entity, ct);
                    }
                    case non_polymer_entity -> {
                        var entryIds = getStructureRepository(ct).getPolymerEntityToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.non_polymer_entity, ct);
                    }
                    case polymer_instance -> {
                        return getStructureRepository(ct).getPolymerEntityToInstance(id);
                    }
                    case assembly -> {
                        var entryIds = getStructureRepository(ct).getPolymerEntityToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.assembly, ct);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getPolymerEntityToComps(id);
                    }
                    case uniprot -> {
                        return getStructureRepository(ct).getPolymerEntityToUniprot(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case branched_entity -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getBranchedEntityToEntry(id);
                    }
                    case branched_instance -> {
                        return getStructureRepository(ct).getBranchedEntityToInstance(id);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getBranchedEntityToComps(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case non_polymer_entity -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getNonPolymerEntityToEntry(id);
                    }
                    case non_polymer_instance -> {
                        return getStructureRepository(ct).getNonPolymerEntityToInstance(id);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getNonPolymerEntityToComps(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case polymer_instance -> {
                switch (to) {
                    case entry -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity(id);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.entry, ct);
                    }
                    case assembly -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity(id);
                        var entryIds = transit(entityIds, Input.Type.polymer_entity, Input.Type.entry, ct);
                        return transit(entryIds, Input.Type.entry, Input.Type.assembly, ct);
                    }
                    case polymer_entity -> {
                        return getStructureRepository(ct).getPolymerInstanceToEntity(id);
                    }
                    case non_polymer_entity -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity(id);
                        var entryIds = transit(entityIds, Input.Type.polymer_entity, Input.Type.entry, ct);
                        return transit(entryIds, Input.Type.entry, Input.Type.non_polymer_entity, ct);
                    }
                    case mol_definition -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity(id);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.mol_definition, ct);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case branched_instance -> {
                switch (to) {
                    case branched_entity -> {
                        return getStructureRepository(ct).getBranchedInstanceToEntity(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case non_polymer_instance -> {
                switch (to) {
                    case non_polymer_entity -> {
                        return getStructureRepository(ct).getNonPolymerInstanceToEntity(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case mol_definition -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getCompsToEntry(id);
                    }
                    case assembly -> {
                        var entryIds = getStructureRepository(ct).getCompsToEntry(id);
                        return transit(entryIds, Input.Type.entry, Input.Type.assembly, ct);
                    }
                    case polymer_entity -> {
                        return getStructureRepository(ct).getCompsToPolymerEntity(id);
                    }
                    case branched_entity -> {
                        return getStructureRepository(ct).getCompsToBranchedEntity(id);
                    }
                    case non_polymer_entity -> {
                        return getStructureRepository(ct).getCompsToNonPolymerEntity(id);
                    }
                    case polymer_instance -> {
                        var entityIds = getStructureRepository(ct).getCompsToPolymerEntity(id);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.polymer_instance, ct);
                    }
                    case drug_bank -> {
                        return getComponentRepository().getCompsToDrugBank(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case group -> {
                switch (to) {
                    case group_provenance -> {
                        return getGroupRepository().getGroupToProvenance(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + from);
        }
    }

    public Collection<String> lookup(String id, Input.AggregationMethod method, Integer cutoff) {
        return getGroupRepository().getMemberToGroup(method, cutoff, id);
    }

    public Collection<String> all(Input.Type from, ContentType ct) {
        switch (from) {
            case entry -> {
                return getAllRepository(ct).getEntryIds();
            }
            case assembly -> {
                return getAllRepository(ct).getAssemblyIds();
            }
            case polymer_entity -> {
                return getAllRepository(ct).getPolymerEntityIds();
            }
            case non_polymer_entity -> {
                return getAllRepository(ct).getNonPolymerEntityIds();
            }
            case polymer_instance -> {
                return getAllRepository(ct).getPolymerInstanceIds();
            }
            case mol_definition -> {
                return getAllRepository(ct).getCompIds();
            }
            default -> throw new IllegalStateException("Unexpected value: " + from);
        }
    }

    public State getState() {
        String error;
        State state = new State();
        if ((error = checkCount(MongoCollections.COLL_ENTRY, getActualCountEntry())) != null)
            state.addError(error);
        if ((error = checkCount(MongoCollections.COLL_POLYMER_ENTITY, getActualCountPolymerEntity())) != null)
            state.addError(error);
        if ((error = checkCount(MongoCollections.COLL_NONPOLYMER_ENTITY, getActualCountNonPolymerEntity())) != null)
            state.addError(error);
        if ((error = checkCount(MongoCollections.COLL_GROUP_POLYMER_ENTITY_SEQUENCE_IDENTITY, getActualCountSequenceGroups())) != null)
            state.addError(error);
        if ((error = checkCount(MongoCollections.COLL_GROUP_POLYMER_ENTITY_UNIPROT_ACCESSION, getActualCountUniprotGroups())) != null)
            state.addError(error);
        if ((error = checkCount(MongoCollections.COLL_GROUP_ENTRY_DEPOSIT_GROUP, getActualCountDepositGroups())) != null)
            state.addError(error);
        return state;
    }

    private String checkCount(String collName, Long actualCount) {
        if (!documentCounts.containsKey(collName))
            return String.format("Count for %s collection was not collected", collName);
        long expectedCount = documentCounts.get(collName);
        if (expectedCount != actualCount)
            return String.format("Collected %d documents from %s collection. Expected total count: %d",
                    actualCount, collName, expectedCount);
        return null;
    }

    private Long getActualCountEntry() {
        return Integer.valueOf(getAllRepository(ContentType.experimental).getEntryIds().size()
                + getAllRepository(ContentType.computational).getEntryIds().size()).longValue();
    }

    private Long getActualCountPolymerEntity() {
        return Integer.valueOf(getAllRepository(ContentType.experimental).getPolymerEntityIds().size()
                + getAllRepository(ContentType.computational).getPolymerEntityIds().size()).longValue();
    }

    private Long getActualCountNonPolymerEntity() {
        return Integer.valueOf(getAllRepository(ContentType.experimental).getNonPolymerEntityIds().size()
                + getAllRepository(ContentType.computational).getNonPolymerEntityIds().size()).longValue();
    }

    private Long getActualCountSequenceGroups() {
        return group.countGroups(Input.AggregationMethod.sequence_identity);
    }

    private Long getActualCountUniprotGroups() {
        return group.countGroups(Input.AggregationMethod.matching_uniprot_accession);
    }

    private Long getActualCountDepositGroups() {
        return group.countGroups(Input.AggregationMethod.matching_deposit_group_id);
    }

    public static class State {
        private final List<String> dataErrors = new ArrayList<>();

        public void addError(String err) {
            dataErrors.add(err);
        }

        public List<String> getDataErrors() {
            return dataErrors;
        }

        public boolean isDataComplete() {
            return dataErrors.isEmpty();
        }
    }
}
