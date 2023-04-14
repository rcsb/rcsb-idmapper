package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_NONPOLYMER_ENTITY;

/**
 *
 * Created on 3/10/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class NonPolymerEntityCollectionSubscriber extends CollectionSubscriber {

    public NonPolymerEntityCollectionSubscriber(Repository r) {
        super(COLL_NONPOLYMER_ENTITY, CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS, r);
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(categoryName, Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
            repository.addNonPolymerEntityToInstance(entry, entity, instances);

            // CCD or BIRD
            String molId = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            repository.addNonPolymerEntityToComps(entity, molId);

        } catch (Exception e) {
            super.onError(e);
        }
    }
}
