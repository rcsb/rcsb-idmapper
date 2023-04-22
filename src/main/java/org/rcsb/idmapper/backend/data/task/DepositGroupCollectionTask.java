package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.input.Input;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_GROUP_ENTRY_DEPOSIT_GROUP;

/**
 * Created on 4/21/23.
 *
 * @author Yana Rose
 */
public class DepositGroupCollectionTask extends CollectionTask {

    List<String> fields = List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS);

    public DepositGroupCollectionTask(Repository r) {
        super(COLL_GROUP_ENTRY_DEPOSIT_GROUP, r);
        setIncludeFields(fields);
    }

    @Override
    Runnable createRunnable(Document document) {
        return () -> {
            Document container = document.get(fields.get(0), Document.class);
            String group = container.getString(CoreConstants.GROUP_ID);
            List<String> members = container.getList(CoreConstants.GROUP_MEMBER_IDS, String.class);
            repository.getGroupRepository()
                    .addGroupMembers(Input.AggregationMethod.matching_deposit_group_id, null, group, members);

        };
    }
}
