package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_POLYMER_ENTITY;

/**
 * For every {@link Document} item that publisher emits, it will parse polymer entity to children mappings,
 * and then update the {@link Repository}.
 *
 * Created on 3/10/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class PolymerEntityCollectionSubscriber extends CollectionSubscriber<Document> {

    public PolymerEntityCollectionSubscriber(Repository r) {
        super(COLL_POLYMER_ENTITY, CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, r);
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(categoryName, Document.class);
            String entity = container.getString(CoreConstants.RCSB_ID);

            List<String> polymerInstances = container.getList(CoreConstants.ASYM_IDS, String.class);
            repository.addPolymerEntityToPolymerInstance(entity, polymerInstances);

            List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
            repository.addPolymerEntityToMonomers(entity, monomers);

            String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            repository.addPolymerEntityToPrd(entity, prd);

        } catch (Exception e) {
            super.onError(e);
        }
    }
}
