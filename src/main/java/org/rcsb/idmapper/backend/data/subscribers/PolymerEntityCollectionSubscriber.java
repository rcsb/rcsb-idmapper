package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.utils.OperationSubscriber;
import org.rcsb.mojave.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
public class PolymerEntityCollectionSubscriber extends OperationSubscriber<Document> {

    private static final Logger logger = LoggerFactory.getLogger(PolymerEntityCollectionSubscriber.class);

    private final Repository repository;

    public PolymerEntityCollectionSubscriber(Repository r) {
        repository = r;
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, Document.class);
            String entity = container.getString(CoreConstants.RCSB_ID);

            List<String> polymerInstances = container.getList(CoreConstants.ASYM_IDS, String.class);
            repository.addPolymerEntityToPolymerInstance(entity, polymerInstances);

            List<String> monomers = container.getList(CoreConstants.CHEM_COMP_MONOMERS, String.class);
            repository.addPolymerEntityToMonomers(entity, monomers);

            String prd = container.getString(CoreConstants.CHEM_REF_DEF_ID);
            repository.addPolymerEntityToPrd(entity, prd);

        } catch (Exception e) {
            logger.error(e.getMessage());
            super.onError(e);
        }
    }
}
