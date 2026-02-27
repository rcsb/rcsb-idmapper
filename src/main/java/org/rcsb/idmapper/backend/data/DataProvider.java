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
import java.util.List;
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

    private final String connectionStringDw;
    private final String connectionStringPdbxCore;
    private final String connectionStringPdbxCompModelCore;

    private MongoDatabase dbDw;
    private MongoDatabase dbPdbxCore;
    private MongoDatabase dbPdbxCompModelCore;

    public DataProvider(String connectionStringDw,
                        String connectionStringPdbxCore,
                        String connectionStringPdbxCompModelCore) {
        this.connectionStringDw = connectionStringDw;
        this.connectionStringPdbxCore = connectionStringPdbxCore;
        this.connectionStringPdbxCompModelCore = connectionStringPdbxCompModelCore;
    }

    public Closeable connect() {
        List<MongoClient> mongoClients = new java.util.ArrayList<>();
        dbDw = connectTo(connectionStringDw, "dw", mongoClients);
        dbPdbxCore = connectTo(connectionStringPdbxCore, "pdbx_core", mongoClients);
        dbPdbxCompModelCore = connectTo(connectionStringPdbxCompModelCore, "pdbx_comp_model_core", mongoClients);
        return () -> {
            for (MongoClient client : mongoClients) {
                client.close();
            }
        };
    }

    public CompletableFuture<Void> initialize(Repository r) {
        logger.info("Initializing data provider");
        if (dbDw == null || dbPdbxCore == null || dbPdbxCompModelCore == null) {
            throw new IllegalStateException("DataProvider not connected. Call connect() before initialize().");
        }

        CompletableFuture<Void> pdbxCoreFuture = runPdbxTasks(dbPdbxCore, r, "pdbx_core");
        CompletableFuture<Void> pdbxCompModelFuture = pdbxCoreFuture.thenCompose(v ->
                runPdbxTasks(dbPdbxCompModelCore, r, "pdbx_comp_model_core"));
        CompletableFuture<Void> dwFuture = runDwTasks(dbDw, r);

        return CompletableFuture.allOf(
                pdbxCompModelFuture,
                dwFuture
        );
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

    private MongoDatabase connectTo(String connectionString, String label, List<MongoClient> clients) {
        var databaseName = new ConnectionString(connectionString).getDatabase();
        logger.info("Connecting to Mongo database ({}) : {}", label, databaseName);

        if (databaseName == null) {
            throw new IllegalArgumentException("Database name must be provided in the connection string URI");
        }
        String mongoUriRedacted = getMongoUriRedacted(connectionString);
        logger.info("Just for easy value checking - mongoUriRedacted ({}): {}", label, mongoUriRedacted);
        logger.info("Just for easy value checking - connectionString ({}): {}", label, connectionString);

        try {
            MongoClient mongoClient = MongoClients.create(connectionString);
            clients.add(mongoClient);
            logger.info("Connected to Mongo database ({}) using: {}", label, mongoUriRedacted);
            return mongoClient.getDatabase(databaseName);
        } catch (Exception e) {
            logger.error("Unable to connect to Mongo database ({}) using: {}", label, mongoUriRedacted);
            throw e;
        }
    }

    private CompletableFuture<Void> runPdbxTasks(MongoDatabase db, Repository r, String label) {
        logger.info("Running PDBX tasks for {}", label);
        List<CollectionTask> tasks = List.of(
                new EntryCollectionTask(r),
                new PolymerEntityCollectionTask(r),
                new BranchedEntityCollectionTask(r),
                new NonPolymerEntityCollectionTask(r)
        );
        return CompletableFuture.allOf(tasks.stream().map(t -> t.findDocuments(db)).toArray(CompletableFuture[]::new));
        //return runTasks(db, tasks);
    }

    private CompletableFuture<Void> runDwTasks(MongoDatabase db, Repository r) {
        logger.info("Running DW tasks");
        List<CollectionTask> tasks = List.of(
                new ComponentsCollectionTask(r),
                new DepositGroupCollectionTask(r),
                new SequenceGroupCollectionTask(r),
                new UniprotGroupCollectionTask(r),
                new ChemCompGroupCollectionTask(r)
        );
        return CompletableFuture.allOf(tasks.stream().map(t -> t.findDocuments(db)).toArray(CompletableFuture[]::new));
        //return runTasks(db, tasks);
    }
/*
    private CompletableFuture<Void> runTasks(MongoDatabase db, List<CollectionTask> tasks) {
        var findFuture = new CompletableFuture<Void>();
        Flux.merge(128, tasks.stream().map(t -> t.findDocuments(db)).toList())
                .subscribe(Runnable::run, findFuture::completeExceptionally, () -> findFuture.complete(null));
        var countFuture = new CompletableFuture<Void>();
        Flux.merge(128, tasks.stream().map(t -> t.countDocuments(db)).toList())
                .subscribe(Runnable::run, countFuture::completeExceptionally, () -> countFuture.complete(null));

        return CompletableFuture.allOf(
                findFuture,
                countFuture
        );
    }
    */
}
