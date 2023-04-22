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

    List<String> fields = List.of(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS);

    public PolymerEntityCollectionTask(Repository r) {
        super(COLL_POLYMER_ENTITY, r);
        setIncludeFields(fields);
    }

    @Override
    Runnable createRunnable(Document document) {
        return () -> {
            Document container = document.get(fields.get(0), Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
            sr.addPolymerEntityToInstance(entry, entity, instances);
            ar.addPolymerInstances(entry, instances);

            List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
            sr.addPolymerEntityToCcd(entity, monomers);
            ar.addComponents(monomers);

            String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            sr.addPolymerEntityToBird(entity, prd);
            ar.addComponents(List.of(prd));

            List<String> uniprots = container.getList(CoreConstants.UNIPROT_IDS, String.class);
            sr.addPolymerEntityToUniprot(entity, uniprots);
        };
    }
}
