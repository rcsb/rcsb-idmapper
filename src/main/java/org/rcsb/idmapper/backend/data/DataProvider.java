package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.backend.data.subscribers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;

import static com.mongodb.client.model.Projections.*;

/**
 * This class is responsible for communication with upstream data source (MongoDb)
 *
 * Possibly iterates over MongoDb collections respecting some filter i.e. should implement Iterable interface
 *
 * May be replaced by simply MongoDb client if too dumb in the end
 *
 * @since 27 Feb 2023
 *
 * @author ingvord
 * @author Yana Rose
 */
public class DataProvider {

    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);

    private MongoDatabase db;

    public Closeable connect() {
        //TODO provide connection string via configuration
        String connectionString = "mongodb://updater:w31teQuerie5@10.20.3.153:27017/dw?authSource=admin&connectTimeoutMS=3000000&socketTimeoutMS=3000000";
        var databaseName = new ConnectionString(connectionString).getDatabase();
        if (databaseName == null)
            throw new IllegalArgumentException("DWH database name MUST be provided in the connection string URI as: mongodb://user:pwd@host:port/db_name?authSource=admin");
        MongoClient mongoClient;
        try {
            mongoClient = MongoClients.create(connectionString);
            logger.info("Connected to MongoDB using: {}", connectionString);
        } catch (Exception e) {
            logger.error("Unable to connect to MongoDB using: {}", connectionString);
            throw e;
        }
        db = mongoClient.getDatabase(databaseName);
        return mongoClient;
    }

    private Flowable<Document> lazyCollectionFetch(CollectionSubscriber s) {
        return Flowable.defer(() -> {
            FindPublisher<Document> publisher = db.getCollection(s.collectionName)
                    .find()
                    .projection(fields(excludeId(), include(s.categoryName)));
            publisher.subscribe(s);
            return Flowable.fromPublisher(publisher)
                    .subscribeOn(Schedulers.io());
        });
    }

    public void initialize(Repository r) {
        logger.info("Starting data provider initialization");
        Instant start = Instant.now();
        Flowable.mergeArray(
                lazyCollectionFetch(new EntryCollectionSubscriber(r)),
                lazyCollectionFetch(new PolymerEntityCollectionSubscriber(r)),
                lazyCollectionFetch(new BranchedEntityCollectionSubscriber(r)),
                lazyCollectionFetch(new NonPolymerEntityCollectionSubscriber(r)),
                lazyCollectionFetch(new ChemComponentsCollectionSubscriber(r))
        ).blockingSubscribe();
        logger.info("Data provider is ready. Time took to initialize: [ {} ] minutes",
                Duration.between(start, Instant.now()).toMinutes());
    }
}
