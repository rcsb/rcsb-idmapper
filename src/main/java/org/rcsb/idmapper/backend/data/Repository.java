package org.rcsb.idmapper.backend.data;

import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.repository.AllRepository;
import org.rcsb.idmapper.backend.data.repository.ComponentRepository;
import org.rcsb.idmapper.backend.data.repository.GroupRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.idmapper.frontend.input.Input;

import java.util.Collection;
import java.util.List;

/**
 * Gateway to concrete implementations of repositories
 *
 * Created on 4/19/23.
 *
 * @author Yana Rose
 */
public class Repository {

    private static final String[] EMPTY_STR_ARRAY = {};

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

    private List<String> transit(Collection<String> ids, Input.Type from, Input.Type to, ContentType ct) {
         return ids.stream()
                 .flatMap(id -> lookup(id, from, to, ct).stream())
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
                    case non_polymer_instance -> {
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
                        return component.getCompsToDrugBank(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case group -> {
                switch (to) {
                    case group_provenance -> {
                        return group.getGroupToProvenance(id);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + from);
        }
    }

    public Collection<String> lookup(String id, Input.AggregationMethod method, Integer cutoff) {
        return group.getMemberToGroup(method, cutoff, id);
    }

    public List<String> all(Input.Type from, ContentType ct) {
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
}
