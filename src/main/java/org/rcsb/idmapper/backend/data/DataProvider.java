package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.rcsb.idmapper.backend.data.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        MongoClient mongoClient;
        String mongoUriRedacted = getMongoUriRedacted(connectionString);
        try {
            mongoClient = MongoClients.create(connectionString);
            logger.info("Connected to Mongo database using: {}", mongoUriRedacted);
        } catch (Exception e) {
            logger.error("Unable to connect to Mongo database using: {}", mongoUriRedacted);
            throw e;
        }
        db = mongoClient.getDatabase(databaseName);
        return mongoClient;
    }

    public enum TaskProfile {
        CORE_PDB,
        CORE_CSM,
        DW
    }

    public CompletableFuture<Void> initialize(Repository r, TaskProfile profile) {
        logger.info("Initializing data provider");
        var findFuture = new CompletableFuture<Void>();
        var tasks = getTasks(profile, r);
        var findPublishers = tasks.stream()
                .map(task -> (org.reactivestreams.Publisher<Runnable>) task.findDocuments(db))
                .toList();
        Flux.merge(findPublishers) //prefetch
                .subscribe(ignored -> {}, findFuture::completeExceptionally, () -> findFuture.complete(null));
        var countFuture = new CompletableFuture<Void>();
        var countPublishers = tasks.stream()
                .map(task -> (org.reactivestreams.Publisher<Runnable>) task.countDocuments(db))
                .toList();
        Flux.merge(countPublishers) //prefetch
                .subscribe(ignored -> {}, countFuture::completeExceptionally, () -> countFuture.complete(null));

        return CompletableFuture.allOf(
                findFuture,
                countFuture
        );
    }

    private java.util.List<CollectionTask> getTasks(TaskProfile profile, Repository r) {
        return switch (profile) {
            case CORE_PDB -> java.util.List.of(
                    new EntryCollectionTask(r),
                    new PolymerEntityCollectionTask(r),
                    new NonPolymerEntityCollectionTask(r),
                    new BranchedEntityCollectionTask(r)
            );
            case CORE_CSM -> java.util.List.of(
                    new EntryCollectionTask(r),
                    new PolymerEntityCollectionTask(r),
                    new NonPolymerEntityCollectionTask(r),
                    new BranchedEntityCollectionTask(r)
            );
            case DW -> java.util.List.of(
                    new ComponentsCollectionTask(r),
                    new DepositGroupCollectionTask(r),
                    new SequenceGroupCollectionTask(r),
                    new UniprotGroupCollectionTask(r),
                    new ChemCompGroupCollectionTask(r)
            );
        };
    }

    public void postInitializationCheck(Repository repository) throws Exception { //TODO special exception class
        Repository.State state = repository.getState();
        if (!state.isDataComplete())
            throw new Exception("Data completeness issue: "+state.getDataErrors());
    }

    /**
     * Redacts the username and password from a MongoDB URI, replacing them with asterisks.
     *
     * @param mongoUri the MongoDB connection URI containing the username and/or password
     * @return a redacted version of the MongoDB URI with the username and password masked
     */
    private String getMongoUriRedacted(String mongoUri) {
        ConnectionString uriObj = new ConnectionString(mongoUri);
        String uriRedacted = mongoUri;
        if (uriObj.getPassword() != null) {
            // getPassword returns the URL-decoded (actual password). To do string replacement we have to do it with the encoded version of it which is what's inside the URI string
            String pwd = String.valueOf(uriObj.getPassword());
            String pwdEncoded = URLEncoder.encode(pwd, StandardCharsets.UTF_8);
            uriRedacted = mongoUri.replace(pwdEncoded, "********");
        }
        if (uriObj.getUsername() != null) {
            uriRedacted = uriRedacted.replace(uriObj.getUsername(), "********");
        }
        return uriRedacted;
    }
}
