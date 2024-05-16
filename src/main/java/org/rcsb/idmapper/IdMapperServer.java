package org.rcsb.idmapper;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.JsonMapper;
import org.rcsb.idmapper.frontend.RSocketFrontendImpl;
import org.rcsb.idmapper.frontend.UndertowFrontendImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class IdMapperServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdMapperServer.class);


    public static final String TRANSLATE = "/translate";
    public static final String GROUP = "/group";
    public static final String ALL = "/all";
    public static final String MONGODB_URI = "MONGODB_URI";

    public static void main(String[] args) {

        var connectionString = Objects.requireNonNull(System.getenv(MONGODB_URI),
                String.format("The environment variable [ %s ] with Mongo database connection string (URI) must be set", MONGODB_URI));
        var backend = new BackendImpl(
                new DataProvider(connectionString),
                new Repository()
        );

        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var mapper = new JsonMapper().create();
        var undertow = new UndertowFrontendImpl<>(backend, AppConfigs.DEFAULT_HTTP_PORT, mapper);
        var rsocket = new RSocketFrontendImpl<>(backend, AppConfigs.DEFAULT_RSOCKET_PORT, mapper);

        try {
            backend.initialize();
            undertow.initialize();
            rsocket.initialize();

            backend.start();
            CompletableFuture.allOf(
                    undertow.start(),
                    rsocket.start()
            ).join();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            backend.stop();
        }
    }
}
