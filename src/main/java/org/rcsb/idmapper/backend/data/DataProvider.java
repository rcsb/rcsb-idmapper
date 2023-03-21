package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.backend.data.subscribers.EntryCollectionSubscriber;
import org.rcsb.idmapper.backend.data.subscribers.PolymerEntityCollectionSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

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

    private static final Logger logger = LoggerFactory.getLogger(DataProvider.class);

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

    public void initialize(Repository r) {
        new CollectionFetcher(new EntryCollectionSubscriber(r), db).run();
        new CollectionFetcher(new PolymerEntityCollectionSubscriber(r), db).run();

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
}
