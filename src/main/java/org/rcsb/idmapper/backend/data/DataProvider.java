package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.client.model.Projections;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.rcsb.common.constants.IdentifierRegex;
import org.rcsb.common.constants.MongoCollections;
import org.rcsb.common.constants.PdbIdFlavor;
import org.rcsb.idmapper.backend.data.task.*;
import org.rcsb.mojave.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.io.Closeable;
import java.util.Optional;
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
 * @author Chunxiao Bi
 * @since 27 Feb 2023
 */
public class DataProvider {

    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private final String connectionString;
    private MongoDatabase db;

    public DataProvider(String connectionString) {
        this.connectionString = connectionString;
    }

    public Closeable connect(DataSource dataSource) {
        var databaseName = new ConnectionString(connectionString).getDatabase();
        if (databaseName == null)
            throw new IllegalArgumentException("Database name must be provided in the connection string URI");
        MongoClient mongoClient = MongoClientProvider.getOrCreate(connectionString, dataSource);
        db = mongoClient.getDatabase(databaseName);
        return MongoClientProvider.noopCloseable();
    }

    public CompletableFuture<Void> initialize(Repository r, DataSource profile) {
        logger.info("Initializing data provider '{}'", db.getName());
        var findFuture = new CompletableFuture<Void>();
        var tasks = getTasks(profile, r);
        var findPublishers = tasks.stream()
                .map(task -> (Publisher<Runnable>) task.findDocuments(db))
                .toList();
        Flux.merge(findPublishers) //prefetch
                .subscribe(Runnable::run, findFuture::completeExceptionally, () -> findFuture.complete(null));
        var countFuture = new CompletableFuture<Void>();
        var countPublishers = tasks.stream()
                .map(task -> (Publisher<Runnable>) task.countDocuments(db))
                .toList();
        Flux.merge(countPublishers) //prefetch
                .subscribe(Runnable::run, countFuture::completeExceptionally, () -> countFuture.complete(null));

        return CompletableFuture.allOf(
                findFuture,
                countFuture
        );
    }

    public PdbIdFlavor detectCorePdbFlavor() {
        if (db == null) {
            throw new IllegalStateException("Cannot detect DB flavor before connection is established");
        }
        try {
            Document sampledDoc = Mono.from(db.getCollection(MongoCollections.COLL_PDBX_CORE_ENTRY)
                            .find()
                            .projection(Projections.include(
                                    CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS + "." + CoreConstants.ENTRY_ID))
                            .first())
                    .block();

            return Optional.ofNullable(sampledDoc)
                    .map(doc -> doc.get(CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, Document.class))
                    .map(ids -> ids.getString(CoreConstants.ENTRY_ID))
                    .map(IdentifierRegex::identifyFlavor)
                    .orElse(PdbIdFlavor.LEGACY);
        } catch (Exception e) {
            String message = "Failed to detect PDB ID flavor from DB " + db.getName();
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private List<CollectionTask> getTasks(DataSource profile, Repository r) {
        return switch (profile) {
            case CORE_PDB -> List.of(
                    new EntryCollectionTask(MongoCollections.COLL_PDBX_CORE_ENTRY, r),
                    new PolymerEntityCollectionTask(MongoCollections.COLL_PDBX_CORE_POLYMER_ENTITY, r),
                    new NonPolymerEntityCollectionTask(MongoCollections.COLL_PDBX_CORE_NONPOLYMER_ENTITY, r),
                    new BranchedEntityCollectionTask(MongoCollections.COLL_PDBX_CORE_BRANCHED_ENTITY, r)
            );
            case CORE_CSM -> List.of(
                    new EntryCollectionTask(MongoCollections.COLL_PDBX_COMP_MODEL_CORE_ENTRY, r),
                    new PolymerEntityCollectionTask(MongoCollections.COLL_PDBX_COMP_MODEL_CORE_POLYMER_ENTITY, r)
            );
            case DW -> List.of(
                    new ComponentsCollectionTask(MongoCollections.COLL_CHEM_COMP, r),
                    new DepositGroupCollectionTask(MongoCollections.COLL_GROUP_METADATA_ENTRY, r),
                    new SequenceGroupCollectionTask(MongoCollections.COLL_GROUP_METADATA_POLYMER_ENTITY, r),
                    new UniprotGroupCollectionTask(MongoCollections.COLL_GROUP_METADATA_POLYMER_ENTITY, r),
                    new ChemCompGroupCollectionTask(MongoCollections.COLL_GROUP_METADATA_NON_POLYMER_ENTITY, r)
            );
        };
    }

    public void postInitializationCheck(Repository repository, DataSource dataSource) {
        Repository.State state = repository.getState(dataSource);
        if (!state.isDataComplete())
            throw new IllegalStateException("Data completeness issue for " + dataSource + ": " + state.getDataErrors());
    }
}
