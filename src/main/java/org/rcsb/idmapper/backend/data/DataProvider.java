package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.rcsb.idmapper.backend.data.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
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
        try {
            mongoClient = MongoClients.create(connectionString);
            logger.info("Connected to Mongo database using: {}", connectionString);
        } catch (Exception e) {
            logger.error("Unable to connect to Mongo database using: {}", connectionString);
            throw e;
        }
        db = mongoClient.getDatabase(databaseName);
        return mongoClient;
    }

    public CompletableFuture<Void> initialize(Repository r) {
        logger.info("Initializing data provider");
        var future = new CompletableFuture<Void>();
        var disposable = Flowable.mergeArray(
                new EntryCollectionTask(r).createFlowable(db),
                new PolymerEntityCollectionTask(r).createFlowable(db),
                new BranchedEntityCollectionTask(r).createFlowable(db),
                new NonPolymerEntityCollectionTask(r).createFlowable(db),
                new ComponentsCollectionTask(r).createFlowable(db),
                new DepositGroupCollectionTask(r).createFlowable(db),
                new SequenceGroupCollectionTask(r).createFlowable(db),
                new UniprotGroupCollectionTask(r).createFlowable(db)
        )
                .doOnNext(runnable -> {
                    System.out.println("From doOnNext:" + Thread.currentThread().getName());
                })
                .subscribe(Runnable::run, future::completeExceptionally, () -> future.complete(null));

        return future;
    }
}
