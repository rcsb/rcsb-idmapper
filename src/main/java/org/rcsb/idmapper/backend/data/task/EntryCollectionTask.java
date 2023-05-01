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

    public EntryCollectionTask(Repository r) {
        super(COLL_ENTRY, r, List.of(
                List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, CoreConstants.ENTRY_ID),
                List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, CoreConstants.PUBMED_ID),
                List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, CoreConstants.ASSEMBLY_IDS),
                List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, CoreConstants.POLYMER_ENTITY_IDS),
                List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, CoreConstants.BRANCHED_ENTITY_IDS),
                List.of(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, CoreConstants.NON_POLYMER_ENTITY_IDS)
        ));
    }

    @Override
    Runnable createRunnable(final Document document) {
        return () -> {

            Document container = document.get(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, Document.class);
            String entry = container.getString(CoreConstants.ENTRY_ID);

            ContentType structureType = getStructureType(entry); // PDB or CSM
            AllRepository ar = repository.getAllRepository(structureType);
            StructureRepository sr = repository.getStructureRepository(structureType);

            ar.addEntry(entry);

            if (container.containsKey(CoreConstants.PUBMED_ID)) {
                Integer pubmed = container.getInteger(CoreConstants.PUBMED_ID);
                sr.addEntryToPubmed(entry, pubmed);
            }

            if (container.containsKey(CoreConstants.ASSEMBLY_IDS)) {
                List<String> assemblies = container.getList(CoreConstants.ASSEMBLY_IDS, String.class);
                sr.addEntryToAssembly(entry, assemblies);
                ar.addAssemblies(entry, assemblies);
            }

            if (container.containsKey(CoreConstants.POLYMER_ENTITY_IDS)) {
                List<String> polymerEntities = container.getList(CoreConstants.POLYMER_ENTITY_IDS, String.class);
                sr.addEntryToPolymerEntity(entry, polymerEntities);
                ar.addPolymerEntities(entry, polymerEntities);
            }

            if (container.containsKey(CoreConstants.BRANCHED_ENTITY_IDS)) {
                List<String> branchedEntities = container.getList(CoreConstants.BRANCHED_ENTITY_IDS, String.class);
                sr.addEntryToBranchedEntity(entry, branchedEntities);
            }

            if (container.containsKey(CoreConstants.NON_POLYMER_ENTITY_IDS)) {
                List<String> nonPolymerEntities = container.getList(CoreConstants.NON_POLYMER_ENTITY_IDS, String.class);
                sr.addEntryToNonPolymerEntity(entry, nonPolymerEntities);
                ar.addNonPolymerEntities(entry, nonPolymerEntities);
            }
        };
    }
}
