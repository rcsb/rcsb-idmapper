package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.idmapper.backend.data.AggregationMethodProvenanceMapper;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.input.Input;
import org.rcsb.mojave.CoreConstants;

import java.util.List;


/**
 * Created on 4/21/23.
 *
 * @author Yana Rose
 */
public class UniprotGroupCollectionTask extends CollectionTask {
    private static final Input.AggregationMethod AGGREGATION_METHOD = Input.AggregationMethod.matching_uniprot_accession;
    private static final String PROVENANCE_ID =
            AggregationMethodProvenanceMapper.toProvenanceId(AGGREGATION_METHOD);
    private static final String GROUP_PROVENANCE_FIELD =
            CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS + "." + CoreConstants.GROUP_PROVENANCE_ID;
    private static final Document FILTER = new Document(
            GROUP_PROVENANCE_FIELD, PROVENANCE_ID);

    public UniprotGroupCollectionTask(String collectionName, Repository r) {
        super(collectionName, r, List.of(
                List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.GROUP_ID),
                List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.GROUP_PROVENANCE_ID),
                List.of(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, CoreConstants.GROUP_MEMBER_IDS)
        ));
    }

    @Override
    Runnable createDocumentRunnable(Document document) {
        return () -> {
            Document container = document.get(CoreConstants.RCSB_GROUP_CONTAINER_IDENTIFIERS, Document.class);
            String group = container.getString(CoreConstants.GROUP_ID);
            String provenance = container.getString(CoreConstants.GROUP_PROVENANCE_ID);
            List<String> members = container.getList(CoreConstants.GROUP_MEMBER_IDS, String.class);

            repository.getGroupRepository().addGroupProvenance(group, provenance);
            repository.getGroupRepository()
                    .addGroupMembers(AggregationMethodProvenanceMapper.toAggregationMethod(provenance), null, group, members);
        };
    }

    @Override
    protected Document getFilter() {
        return FILTER;
    }

    @Override
    protected String getFilterLogDetails() {
        return CoreConstants.GROUP_PROVENANCE_ID + "=" + PROVENANCE_ID;
    }

    @Override
    public Runnable createCountRunnable(Long count) {
        return () -> repository.addCount(
                Repository.groupMetadataCountKey(PROVENANCE_ID),
                count
        );
    }
}
