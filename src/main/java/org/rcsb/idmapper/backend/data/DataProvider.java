package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.rcsb.common.constants.MongoCollections;
import org.rcsb.idmapper.backend.data.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import java.util.List;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * This class is responsible for communication with upstream data source (MongoDb)
 * <p>
 * Possibly iterates over MongoDb collections respecting some filter i.e. should implement Iterable interface
 * <p>
 * May be replaced by simply MongoDb client if too dumb in the end
 *
 * @author ingvord
 * @author Yana Rose
 * @since 27 Feb 2023
 */
public class DataProvider {

    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private final String connectionString;
    private MongoDatabase db;

    public DataProvider(String connectionString) {
        this.connectionString = connectionString;
    }

    public Closeable connect() {
        var databaseName = new ConnectionString(connectionString).getDatabase();
        if (databaseName == null)
            throw new IllegalArgumentException("Database name must be provided in the connection string URI");
        MongoClient mongoClient = MongoClientProvider.getOrCreate(connectionString);
        db = mongoClient.getDatabase(databaseName);
        return MongoClientProvider.noopCloseable();
    }

    public enum TaskProfile {
        CORE_PDB,
        CORE_CSM,
        DW
    }

    public CompletableFuture<Void> initialize(Repository r, TaskProfile profile) {
        logger.info("Initializing data provider '{}'", db.getName());
        var findFuture = new CompletableFuture<Void>();
        var tasks = getTasks(profile, r);
        var findPublishers = tasks.stream()
                .map(task -> (org.reactivestreams.Publisher<Runnable>) task.findDocuments(db))
                .toList();
        Flux.merge(findPublishers) //prefetch
                .subscribe(Runnable::run, findFuture::completeExceptionally, () -> findFuture.complete(null));
        var countFuture = new CompletableFuture<Void>();
        var countPublishers = tasks.stream()
                .map(task -> (org.reactivestreams.Publisher<Runnable>) task.countDocuments(db))
                .toList();
        Flux.merge(countPublishers) //prefetch
                .subscribe(Runnable::run, countFuture::completeExceptionally, () -> countFuture.complete(null));

        return CompletableFuture.allOf(
                findFuture,
                countFuture
        );
    }

    private List<CollectionTask> getTasks(TaskProfile profile, Repository r) {
        return switch (profile) {
            case CORE_PDB -> List.of(
                    new EntryCollectionTask(MongoCollections.COLL_PDBX_CORE_ENTRY, r, org.rcsb.common.constants.ContentType.experimental),
                    new PolymerEntityCollectionTask(MongoCollections.COLL_PDBX_CORE_POLYMER_ENTITY, r, org.rcsb.common.constants.ContentType.experimental),
                    new NonPolymerEntityCollectionTask(MongoCollections.COLL_PDBX_CORE_NONPOLYMER_ENTITY, r, org.rcsb.common.constants.ContentType.experimental),
                    new BranchedEntityCollectionTask(MongoCollections.COLL_PDBX_CORE_BRANCHED_ENTITY, r, org.rcsb.common.constants.ContentType.experimental)
            );
            case CORE_CSM -> List.of(
                    new EntryCollectionTask(MongoCollections.COLL_PDBX_COMP_MODEL_CORE_ENTRY, r, org.rcsb.common.constants.ContentType.computational),
                    new PolymerEntityCollectionTask(MongoCollections.COLL_PDBX_COMP_MODEL_CORE_POLYMER_ENTITY, r, org.rcsb.common.constants.ContentType.computational)
            );
            case DW -> List.of(
                    new ComponentsCollectionTask(MongoCollections.COLL_CHEM_COMP, r),
                    new DepositGroupCollectionTask(MongoCollections.COLL_GROUP_ENTRY_DEPOSIT_GROUP, r),
                    new SequenceGroupCollectionTask(MongoCollections.COLL_GROUP_POLYMER_ENTITY_SEQUENCE_IDENTITY, r),
                    new UniprotGroupCollectionTask(MongoCollections.COLL_GROUP_POLYMER_ENTITY_UNIPROT_ACCESSION, r),
                    new ChemCompGroupCollectionTask(MongoCollections.COLL_GROUP_NON_POLYMER_ENTITY_CHEMICAL_COMPONENT, r)
            );
        };
    }

    public void postInitializationCheck(Repository repository, TaskProfile taskProfile) throws Exception { //TODO special exception class
        Repository.State state = repository.getState(taskProfile);
        if (!state.isDataComplete())
            throw new IllegalStateException("Data completeness issue for " + taskProfile + ": " + state.getDataErrors());
    }

}
