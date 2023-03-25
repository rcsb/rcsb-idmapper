package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.mojave.CoreConstants;

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

    public EntryCollectionSubscriber(Repository r) {
        super(COLL_ENTRY, CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, r);
    }

    @Override
    public void onNext(final Document document) {
        try {
            Document container = document.get(categoryName, Document.class);
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
            super.onError(e);
        }
    }
}
