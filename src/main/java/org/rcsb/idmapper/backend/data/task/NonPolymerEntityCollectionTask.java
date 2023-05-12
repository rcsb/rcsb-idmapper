package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.backend.data.repository.AllRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_NONPOLYMER_ENTITY;

/**
 *
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public class NonPolymerEntityCollectionTask extends CollectionTask {

    public NonPolymerEntityCollectionTask(Repository r) {
        super(COLL_NONPOLYMER_ENTITY, r, List.of(
                List.of(CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.RCSB_ID),
                List.of(CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.ENTRY_ID),
                List.of(CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.ASYM_IDS),
                List.of(CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.CHEM_REF_DEF_ID)
        ));
    }

    @Override
    Runnable createDocumentRunnable(final Document document) {
        return () -> {
            Document container = document.get(CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS, Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            if (container.containsKey(CoreConstants.ASYM_IDS)) {
                List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
                sr.addNonPolymerEntityToInstance(entry, entity, instances);
            }

            if (container.containsKey(CoreConstants.CHEM_REF_DEF_ID)) {
                String compId = container.getString(CoreConstants.CHEM_REF_DEF_ID);
                sr.addNonPolymerEntityToComps(entity, compId);
                sr.addEntryToComps(entry, List.of(compId));
                ar.addComponents(List.of(compId));
            }
        };
    }
}
