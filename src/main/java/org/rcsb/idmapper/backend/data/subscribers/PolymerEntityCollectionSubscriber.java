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
public class PolymerEntityCollectionSubscriber extends CollectionSubscriber {

    public PolymerEntityCollectionSubscriber(Repository r) {
        super(COLL_POLYMER_ENTITY, CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, r);
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(categoryName, Document.class);

            String entry = container.getString(CoreConstants.ENTRY_ID);
            String entity = container.getString(CoreConstants.RCSB_ID);

            List<String> instances = container.getList(CoreConstants.ASYM_IDS, String.class);
            repository.addPolymerEntityToInstance(entry, entity, instances);

            List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
            repository.addPolymerEntityToCcd(entity, monomers);

            String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            repository.addPolymerEntityToBird(entity, prd);

            List<String> uniprots = container.getList(CoreConstants.UNIPROT_IDS, String.class);
            repository.addPolymerEntityToUniprot(entity, uniprots);

        } catch (Exception e) {
            super.onError(e);
        }
    }
}
