package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.input.Input;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_GROUP_POLYMER_ENTITY_UNIPROT_ACCESSION;

/**
 * Created on 4/21/23.
 *
 * @author Yana Rose
 */
public class UniprotGroupCollectionTask extends CollectionTask {

    public UniprotGroupCollectionTask(Repository r) {
        super(COLL_GROUP_POLYMER_ENTITY_UNIPROT_ACCESSION, r, List.of(
                List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.GROUP_ID),
                List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.GROUP_PROVENANCE_ID),
                List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.GROUP_MEMBER_IDS)
        ));
    }

    @Override
    Runnable createRunnable(Document document) {
        return () -> {
            Document container = document.get(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, Document.class);
            String group = container.getString(CoreConstants.GROUP_ID);
            String provenance = container.getString(CoreConstants.GROUP_PROVENANCE_ID);
            List<String> members = container.getList(CoreConstants.GROUP_MEMBER_IDS, String.class);

            repository.getGroupRepository().addGroupProvenance(group, provenance);
            repository.getGroupRepository()
                    .addGroupMembers(Input.AggregationMethod.matching_uniprot_accession, null, group, members);
        };
    }
}
