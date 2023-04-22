package org.rcsb.idmapper.backend.data.task;

import org.bson.Document;
import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.backend.data.repository.AllRepository;
import org.rcsb.idmapper.backend.data.repository.StructureRepository;
import org.rcsb.mojave.CoreConstants;

import java.util.List;

import static org.rcsb.common.constants.MongoCollections.COLL_ENTRY;

/**
 * For every {@link Document} item that publisher emits, it will parse entry to children mappings,
 * and then update the {@link StructureRepository}.
 *
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public class EntryCollectionTask extends CollectionTask {

    List<String> fields = List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS);

    public EntryCollectionTask(Repository r) {
        super(COLL_ENTRY, r);
        setIncludeFields(fields);
    }

    @Override
    Runnable createRunnable(final Document document) {
        return () -> {

            Document container = document.get(this.fields.get(0), Document.class);
            String entry = container.getString(CoreConstants.ENTRY_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            ar.addEntry(entry);

            Integer pubmed = container.getInteger(CoreConstants.PUBMED_ID);
            sr.addEntryToPubmed(entry, pubmed);

            List<String> assemblies = container.getList(CoreConstants.ASSEMBLY_IDS, String.class);
            sr.addEntryToAssembly(entry, assemblies);
            ar.addAssemblies(entry, assemblies);

            List<String> polymerEntities = container.getList(CoreConstants.POLYMER_ENTITY_IDS, String.class);
            sr.addEntryToPolymerEntity(entry, polymerEntities);
            ar.addPolymerEntities(entry, polymerEntities);

            List<String> branchedEntities = container.getList(CoreConstants.BRANCHED_ENTITY_IDS, String.class);
            sr.addEntryToBranchedEntity(entry, branchedEntities);

            List<String> nonPolymerEntities = container.getList(CoreConstants.NON_POLYMER_ENTITY_IDS, String.class);
            sr.addEntryToNonPolymerEntity(entry, nonPolymerEntities);
            ar.addNonPolymerEntities(entry, nonPolymerEntities);
        };
    }
}
