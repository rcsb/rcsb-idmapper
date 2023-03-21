package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.utils.CollectionSubscriber;
import org.rcsb.mojave.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_ENTRY;

/**
 * For every {@link Document} item that publisher emits, it will parse entry to children mappings,
 * and then update the {@link Repository}.
 *
 * Created on 3/10/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class EntryCollectionSubscriber extends CollectionSubscriber<Document> {

    private static final Logger logger = LoggerFactory.getLogger(EntryCollectionSubscriber.class);

    private final Repository repository;

    public EntryCollectionSubscriber(Repository r) {
        repository = r;
        collectionName = COLL_ENTRY;
        categoryName = CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS;
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, Document.class);
            String entry = container.getString(CoreConstants.ENTRY_ID);

            Integer pubmed = container.getInteger(CoreConstants.PUBMED_ID);
            repository.addEntryToPubmedMapping(entry, pubmed);

            List<String> assemblies = container.getList(CoreConstants.ASSEMBLY_IDS, String.class);
            repository.addEntryToAssemblyMapping(entry, assemblies);

            List<String> polymerEntities = container.getList(CoreConstants.POLYMER_ENTITY_IDS, String.class);
            repository.addEntryToPolymerEntity(entry, polymerEntities);

            List<String> branchedEntities = container.getList(CoreConstants.BRANCHED_ENTITY_IDS, String.class);
            repository.addEntryToBranchedEntity(entry, branchedEntities);

            List<String> nonPolymerEntities = container.getList(CoreConstants.NON_POLYMER_ENTITY_IDS, String.class);
            repository.addEntryToNonPolymerEntity(entry, nonPolymerEntities);

        } catch (Exception e) {
            logger.error(e.getMessage());
            super.onError(e);
        }
    }
}
