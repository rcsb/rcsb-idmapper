package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.backend.data.repository.AllRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_BRANCHED_ENTITY;

/**
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public class BranchedEntityCollectionTask extends CollectionTask {

    public BranchedEntityCollectionTask(Repository r) {
        super(COLL_BRANCHED_ENTITY, r, List.of(
                List.of(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.RCSB_ID),
                List.of(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.ENTRY_ID),
                List.of(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.ASYM_IDS),
                List.of(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.CHEM_COMP_MONOMERS),
                List.of(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, CoreConstants.CHEM_REF_DEF_ID)
        ));
    }

    @Override
    Runnable createRunnable(final Document document) {
        return () -> {
            Document container = document.get(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            if (container.containsKey(CoreConstants.ASYM_IDS)) {
                List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
                sr.addBranchedEntityToInstance(entry, entity, instances);
            }

            if (container.containsKey(CoreConstants.CHEM_COMP_MONOMERS)) {
                List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
                sr.addBranchedEntityToCcd(entity, monomers);
                sr.addEntryToComps(entry, monomers);
                ar.addComponents(monomers);
            }

            if (container.containsKey(CoreConstants.CHEM_REF_DEF_ID)) {
                String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
                sr.addBranchedEntityToBird(entity, prd);
                sr.addEntryToComps(entry, List.of(prd));
                ar.addComponents(List.of(prd));
            }
        };
    }
}
