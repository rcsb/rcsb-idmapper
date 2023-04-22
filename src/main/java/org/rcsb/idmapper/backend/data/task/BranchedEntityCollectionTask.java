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

    List<String> fields = List.of(CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS);

    public BranchedEntityCollectionTask(Repository r) {
        super(COLL_BRANCHED_ENTITY, r);
        setIncludeFields(fields);
    }

    @Override
    Runnable createRunnable(final Document document) {
        return () -> {
            Document container = document.get(fields.get(0), Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
            sr.addBranchedEntityToInstance(entry, entity, instances);

            List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
            sr.addBranchedEntityToCcd(entity, monomers);
            ar.addComponents(monomers);

            String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            sr.addBranchedEntityToBird(entity, prd);
            ar.addComponents(List.of(prd));
        };
    }
}
