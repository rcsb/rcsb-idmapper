package org.rcsb.idmapper.backend.data;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A MongoClient singleton for connecting to three databases.
 *
 * @since March 5, 2026
 * @author Chunxiao
 */
final class MongoClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);
    private static final ConcurrentHashMap<DataProvider.TaskProfile, MongoClient> CLIENTS = new ConcurrentHashMap<>();
    private static final Closeable NOOP_CLOSEABLE = () -> { };

    private MongoClientProvider() {
    }

    static MongoClient getOrCreate(String connectionString, DataProvider.TaskProfile taskProfile) {
        return CLIENTS.computeIfAbsent(taskProfile, key -> {
            String mongoUriRedacted = getMongoUriRedacted(connectionString);
            try {
                MongoClient created = MongoClients.create(connectionString);
                logger.info("Connected to Mongo database using: {}", mongoUriRedacted);
                return created;
            } catch (Exception e) {
                logger.error("Unable to connect to Mongo database using: {}", mongoUriRedacted);
                throw e;
            }
        });
    }

    static Closeable noopCloseable() {
        return NOOP_CLOSEABLE;
    }

    private static String getMongoUriRedacted(String mongoUri) {
        ConnectionString uriObj = new ConnectionString(mongoUri);
        String uriRedacted = mongoUri;
        if (uriObj.getPassword() != null) {
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
