package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_BRANCHED_ENTITY;

/**
 * Created on 3/10/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class BranchedEntityCollectionSubscriber extends CollectionSubscriber {

    public BranchedEntityCollectionSubscriber(Repository r) {
        super(COLL_BRANCHED_ENTITY, CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS, r);
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(categoryName, Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
            repository.addBranchedEntityToInstance(entry, entity, instances);

            List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
            repository.addBranchedEntityToCcd(entity, monomers);

            String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            repository.addBranchedEntityToBird(entity, prd);

        } catch (Exception e) {
            super.onError(e);
        }
    }
}
