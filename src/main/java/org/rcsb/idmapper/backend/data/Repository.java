package org.rcsb.idmapper.backend.data;

import io.reactivex.rxjava3.core.Observable;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.repository.*;
import org.rcsb.idmapper.frontend.input.Input;

import java.util.List;

/**
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
        return ct.equals(ContentType.experimental)
                ? allExperimental
                : allComputational;
    }

    public StructureRepository getStructureRepository(ContentType ct) {
        return ct.equals(ContentType.experimental)
                ? structureExperimental
                : structureComputational;
    }

    public GroupRepository getGroupRepository() {
        return group;
    }

    public ComponentRepository getComponentRepository() {
        return component;
    }

    private String[] transit(String[] ids, Input.Type from, Input.Type to, ContentType ct) {
         return Observable.fromArray(ids)
                 .flatMap(id -> Observable.fromArray(lookup(id, from, to, ct)))
                 .toList()
                 .blockingGet()
                 .toArray(String[]::new);
    }

    public String[] lookup(String id, Input.Type from, Input.Type to, ContentType ct) {
        switch (from) {
            case entry -> {
                switch (to) {
                    case assembly -> {
                        return getStructureRepository(ct).getEntryToAssembly().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case polymer_entity -> {
                        return getStructureRepository(ct).getEntryToPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case branched_entity -> {
                        return getStructureRepository(ct).getEntryToBranchedEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case non_polymer_entity -> {
                        return getStructureRepository(ct).getEntryToNonPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case polymer_instance -> {
                        var entityIds = getStructureRepository(ct).getEntryToPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.polymer_instance, ct);
                    }
                    case branched_instance -> {
                        var entityIds = getStructureRepository(ct).getEntryToBranchedEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.branched_entity, Input.Type.branched_instance, ct);
                    }
                    case non_polymer_instance -> {
                        var entityIds = getStructureRepository(ct).getEntryToNonPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.non_polymer_entity, Input.Type.non_polymer_instance, ct);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getEntryToComps().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case drug_bank -> {
                        var compIds = getStructureRepository(ct).getEntryToComps().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(compIds, Input.Type.mol_definition, Input.Type.drug_bank, ct);
                    }
                    case pubmed -> {
                        return getStructureRepository(ct).getEntryToPubmed().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case uniprot -> {
                        var entityIds = getStructureRepository(ct).getEntryToPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.uniprot, ct);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case assembly -> {
                var entryIds = getStructureRepository(ct).getAssemblyToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                switch (to) {
                    case entry -> {
                        return entryIds;
                    }
                    case polymer_entity -> {
                        return transit(entryIds, Input.Type.entry, Input.Type.polymer_entity, ct);
                    }
                    case polymer_instance -> {
                        var entityIds = transit(entryIds, Input.Type.entry, Input.Type.polymer_entity, ct);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.polymer_instance, ct);
                    }
                    case non_polymer_entity -> {
                        return transit(entryIds, Input.Type.entry, Input.Type.non_polymer_entity, ct);
                    }
                    case mol_definition -> {
                        return transit(entryIds, Input.Type.entry, Input.Type.mol_definition, ct);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case polymer_entity -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getPolymerEntityToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case branched_entity -> {
                        var entryIds = getStructureRepository(ct).getPolymerEntityToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entryIds, Input.Type.entry, Input.Type.branched_entity, ct);
                    }
                    case non_polymer_entity -> {
                        var entryIds = getStructureRepository(ct).getPolymerEntityToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entryIds, Input.Type.entry, Input.Type.non_polymer_entity, ct);
                    }
                    case polymer_instance -> {
                        return getStructureRepository(ct).getPolymerEntityToInstance().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getPolymerEntityToComps().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case uniprot -> {
                        return getStructureRepository(ct).getPolymerEntityToUniprot().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case branched_entity -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getBranchedEntityToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case non_polymer_instance -> {
                        return getStructureRepository(ct).getBranchedEntityToInstance().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getBranchedEntityToComps().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case non_polymer_entity -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getNonPolymerEntityToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case non_polymer_instance -> {
                        return getStructureRepository(ct).getNonPolymerEntityToInstance().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case mol_definition -> {
                        return getStructureRepository(ct).getNonPolymerEntityToComps().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case polymer_instance -> {
                switch (to) {
                    case entry -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.entry, ct);
                    }
                    case assembly -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        var entryIds = transit(entityIds, Input.Type.polymer_entity, Input.Type.entry, ct);
                        return transit(entryIds, Input.Type.entry, Input.Type.assembly, ct);
                    }
                    case polymer_entity -> {
                        return getStructureRepository(ct).getPolymerInstanceToEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case mol_definition -> {
                        var entityIds = getStructureRepository(ct).getPolymerInstanceToEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.mol_definition, ct);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case branched_instance -> {
                switch (to) {
                    case branched_entity -> {
                        return getStructureRepository(ct).getBranchedInstanceToEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case non_polymer_instance -> {
                switch (to) {
                    case non_polymer_entity -> {
                        return getStructureRepository(ct).getNonPolymerInstanceToEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case mol_definition -> {
                switch (to) {
                    case entry -> {
                        return getStructureRepository(ct).getCompsToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case assembly -> {
                        var entryIds = getStructureRepository(ct).getCompsToEntry().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entryIds, Input.Type.entry, Input.Type.assembly, ct);
                    }
                    case polymer_entity -> {
                        return getStructureRepository(ct).getCompsToPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case branched_entity -> {
                        return getStructureRepository(ct).getCompsToBranchedEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case non_polymer_entity -> {
                        return getStructureRepository(ct).getCompsToNonPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    case polymer_instance -> {
                        var entityIds = getStructureRepository(ct).getCompsToPolymerEntity().getOrDefault(id, EMPTY_STR_ARRAY);
                        return transit(entityIds, Input.Type.polymer_entity, Input.Type.polymer_instance, ct);
                    }
                    case drug_bank -> {
                        return component.getCompsToDrugBank().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            case group -> {
                switch (to) {
                    case group_provenance -> {
                        return group.getGroupToProvenance().getOrDefault(id, EMPTY_STR_ARRAY);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + to);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + from);
        }
    }

    public String[] lookup(String id, Input.AggregationMethod method, Integer cutoff) {
        return group.getMemberToGroup(method, cutoff).getOrDefault(id, EMPTY_STR_ARRAY);
    }

    public String[] all(Input.Type from, ContentType ct) {
        List<String> ids;
        switch (from) {
            case entry -> ids = getAllRepository(ct).getEntryIds();
            case assembly -> ids = getAllRepository(ct).getAssemblyIds();
            case polymer_entity -> ids = getAllRepository(ct).getPolymerEntityIds();
            case non_polymer_entity -> ids = getAllRepository(ct).getNonPolymerEntityIds();
            case polymer_instance -> ids = getAllRepository(ct).getPolymerInstanceIds();
            case mol_definition -> ids = getAllRepository(ct).getCompIds();
            default -> throw new IllegalStateException("Unexpected value: " + from);
        }
        return ids.toArray(String[]::new);
    }
}
