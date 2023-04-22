package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.input.Input;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_GROUP_POLYMER_ENTITY_SEQUENCE_IDENTITY;

/**
 * Created on 4/21/23.
 *
 * @author Yana Rose
 */
public class SequenceGroupCollectionTask extends CollectionTask {

    List<String> fields = List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.RCSB_GROUP_STATISTICS);

    public SequenceGroupCollectionTask(Repository r) {
        super(COLL_GROUP_POLYMER_ENTITY_SEQUENCE_IDENTITY, r);
        setIncludeFields(fields);
    }

    @Override
    Runnable createRunnable(Document document) {
        return () -> {
            Document container = document.get(fields.get(0), Document.class);
            String group = container.getString(CoreConstants.GROUP_ID);
            List<String> members = container.getList(CoreConstants.GROUP_MEMBER_IDS, String.class);

            Document stats = document.get(fields.get(1), Document.class);
            Integer cutoff = stats.getInteger(CoreConstants.SIMILARITY_CUTOFF);

            repository.getGroupRepository()
                    .addGroupMembers(Input.AggregationMethod.sequence_identity, cutoff, group, members);
        };
    }
}
