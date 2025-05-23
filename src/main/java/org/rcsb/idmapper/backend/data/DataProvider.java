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

    public CompletableFuture<Void> initialize(Repository r) {
        logger.info("Initializing data provider");
        var findFuture = new CompletableFuture<Void>();
        Flux.merge(128,//prefetch
                        new EntryCollectionTask(r).findDocuments(db),
                        new PolymerEntityCollectionTask(r).findDocuments(db),
                        new BranchedEntityCollectionTask(r).findDocuments(db),
                        new NonPolymerEntityCollectionTask(r).findDocuments(db),
                        new ComponentsCollectionTask(r).findDocuments(db),
                        new DepositGroupCollectionTask(r).findDocuments(db),
                        new SequenceGroupCollectionTask(r).findDocuments(db),
                        new UniprotGroupCollectionTask(r).findDocuments(db),
                        new ChemCompGroupCollectionTask(r).findDocuments(db)
                )
                .subscribe(Runnable::run, findFuture::completeExceptionally, () -> findFuture.complete(null));
        var countFuture = new CompletableFuture<Void>();
        Flux.merge(128,//prefetch
                        new EntryCollectionTask(r).countDocuments(db),
                        new PolymerEntityCollectionTask(r).countDocuments(db),
                        new BranchedEntityCollectionTask(r).countDocuments(db),
                        new NonPolymerEntityCollectionTask(r).countDocuments(db),
                        new ComponentsCollectionTask(r).countDocuments(db),
                        new DepositGroupCollectionTask(r).countDocuments(db),
                        new SequenceGroupCollectionTask(r).countDocuments(db),
                        new UniprotGroupCollectionTask(r).countDocuments(db),
                        new ChemCompGroupCollectionTask(r).countDocuments(db)
                )
                .subscribe(Runnable::run, countFuture::completeExceptionally, () -> countFuture.complete(null));

        return CompletableFuture.allOf(
                findFuture,
                countFuture
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

}
