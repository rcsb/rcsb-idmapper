package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.mojave.CoreConstants;

import static org.rcsb.common.constants.MongoCollections.COLL_CHEM_COMP;
import static org.rcsb.common.constants.MongoCollections.COLL_POLYMER_ENTITY_INSTANCE;

/**
 *
 * Created on 3/10/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class ChemComponentsCollectionSubscriber extends CollectionSubscriber {

    public ChemComponentsCollectionSubscriber(Repository r) {
        super(COLL_CHEM_COMP, CoreConstants.RCSB_CHEM_COMP_CONTAINER_IDENTIFIERS, r);
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(categoryName, Document.class);
            String comp = container.getString(CoreConstants.COMP_ID);
            String drug = container.getString(CoreConstants.DRUGBANK_ID);
            repository.addChemCompsToDrugBank(comp, drug);
        } catch (Exception e) {
            super.onError(e);
        }
    }
}
