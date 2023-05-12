package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.backend.data.repository.AllRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_POLYMER_ENTITY;

/**
 * For every {@link Document} item that publisher emits, it will parse polymer entity to children mappings,
 * and then update the {@link Repository}.
 *
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public class PolymerEntityCollectionTask extends CollectionTask {

    public PolymerEntityCollectionTask(Repository r) {
        super(COLL_POLYMER_ENTITY, r, List.of(
                List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.RCSB_ID),
                List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.ENTRY_ID),
                List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.ASYM_IDS),
                List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.CHEM_COMP_MONOMERS),
                List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.CHEM_REF_DEF_ID),
                List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.UNIPROT_IDS)
        ));
    }

    @Override
    Runnable createDocumentRunnable(Document document) {
        return () -> {
            Document container = document.get(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            if (container.containsKey(CoreConstants.ASYM_IDS)) {
                List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
                sr.addPolymerEntityToInstance(entry, entity, instances);
                ar.addPolymerInstances(entry, instances);
            }

            if (container.containsKey(CoreConstants.CHEM_COMP_MONOMERS)) {
                List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
                sr.addPolymerEntityToCcd(entity, monomers);
                sr.addEntryToComps(entry, monomers);
                ar.addComponents(monomers);
            }

            if (container.containsKey(CoreConstants.CHEM_REF_DEF_ID)) {
                String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
                sr.addPolymerEntityToBird(entity, prd);
                sr.addEntryToComps(entry, List.of(prd));
                ar.addComponents(List.of(prd));
            }

            if (container.containsKey(CoreConstants.UNIPROT_IDS)) {
                List<String> uniprots = container.getList(CoreConstants.UNIPROT_IDS, String.class);
                sr.addPolymerEntityToUniprot(entity, uniprots);
            }
        };
    }
}
