package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_CHEM_COMP;

/**
 *
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public class ComponentsCollectionTask extends CollectionTask {

    List<String> fields = List.of(CoreConstants.RCSB_CHEM_COMP_CONTAINER_IDENTIFIERS);

    public ComponentsCollectionTask(Repository r) {
        super(COLL_CHEM_COMP, r);
        setIncludeFields(fields);
    }

    @Override
    Runnable createRunnable(final Document document) {
        return () -> {
            Document container = document.get(fields.get(0), Document.class);
            String comp = container.getString(CoreConstants.COMP_ID);
            if (container.containsKey(CoreConstants.DRUGBANK_ID)) {
                String drug = container.getString(CoreConstants.DRUGBANK_ID);
                repository.getComponentRepository().addChemCompsToDrugBank(comp, drug);
            }
        };
    }
}