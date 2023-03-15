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
import org.rcsb.idmapper.backend.data.subscribers.EntryCollectionSubscriber;
import org.rcsb.idmapper.backend.data.subscribers.PolymerEntityCollectionSubscriber;
import org.rcsb.idmapper.utils.OperationSubscriber;
import org.rcsb.mojave.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

import static com.mongodb.client.model.Projections.*;
import static org.rcsb.common.constants.MongoCollections.COLL_ENTRY;
import static org.rcsb.common.constants.MongoCollections.COLL_POLYMER_ENTITY;

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
public class DataProvider implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(DataProvider.class);

    private MongoDatabase db;
    private MongoClient mongoClient;
    private boolean connectionOpened;

    public void connect() {
        //TODO provide connection string via configuration
        String connectionString = "mongodb://updater:w31teQuerie5@10.20.3.153:27017/dw?authSource=admin&connectTimeoutMS=3000000&socketTimeoutMS=3000000";
        var databaseName = new ConnectionString(connectionString).getDatabase();
        if (databaseName == null)
            throw new IllegalArgumentException("DWH database name MUST be provided in the connection string URI as: mongodb://user:pwd@host:port/db_name?authSource=admin");
        try {
            mongoClient = MongoClients.create(connectionString);
            connectionOpened = true;
            logger.info("Connected to MongoDB using: {}", connectionString);
        } catch (Exception e) {
            logger.error("Unable to connect to MongoDB using: {}", connectionString);
            throw e;
        }
        db = mongoClient.getDatabase(databaseName);
    }

    private void fetchFromCollection(String collName, String containerName, OperationSubscriber<Document> worker) {
        FindPublisher<Document> publisher = db.getCollection(collName)
                .find()
                .projection(fields(excludeId(), include(containerName)));
        Flowable.fromPublisher(publisher)
                .subscribeOn(Schedulers.io())
                .subscribe(worker);
        worker.run();
    }

    public void initialize(Repository repository) {
        streamEntryCollection(repository);
        streamPolymerEntityCollection(repository);
        System.out.println("ok");
    }

    private void streamEntryCollection(Repository r) {
        OperationSubscriber<Document> s = new EntryCollectionSubscriber(r);
        fetchFromCollection(COLL_ENTRY, CoreConstants.RCSB_ENTRY_CONTAINER_IDENTIFIERS, s);
    }

    private void streamPolymerEntityCollection(Repository r) {
        OperationSubscriber<Document> s = new PolymerEntityCollectionSubscriber(r);
        fetchFromCollection(COLL_POLYMER_ENTITY, CoreConstants.RCSB_POLYMER_ENTITY_CONTAINER_IDENTIFIERS, s);
    }

    private void streamBranchedEntityCollection(Repository r) {
        //COLL_BRANCHED_ENTITY,
        //CoreConstants.RCSB_BRANCHED_ENTITY_CONTAINER_IDENTIFIERS;
    }

    private void streamNonPolymerEntityCollection(Repository r) {
        //COLL_NONPOLYMER_ENTITY,
        //CoreConstants.RCSB_NONPOLYMER_ENTITY_CONTAINER_IDENTIFIERS
    }

    private void streamPolymerInstanceCollection(Repository r) {
        //COLL_POLYMER_ENTITY_INSTANCE,
        //CoreConstants.RCSB_POLYMER_ENTITY_INSTANCE_CONTAINER_IDENTIFIERS
    }

    private void subscribeToBranchedInstance(Repository r) {
        //COLL_BRANCHED_ENTITY_INSTANCE,
        //CoreConstants.RCSB_BRANCHED_ENTITY_INSTANCE_CONTAINER_IDENTIFIERS
    }

    private void subscribeToNonPolymerInstance(Repository r) {
        //COLL_NONPOLYMER_ENTITY_INSTANCE,
        //CoreConstants.RCSB_NONPOLYMER_ENTITY_INSTANCE_CONTAINER_IDENTIFIERS
    }

    @Override
    public void close() {
        if (connectionOpened) { // release resources
            mongoClient.close();
            logger.info("MongoDB connection is closed");
        }
    }
}
