package org.rcsb.idmapper;

import org.rcsb.common.constants.ContentType;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.JsonMapper;
import org.rcsb.idmapper.frontend.RSocketFrontendImpl;
import org.rcsb.idmapper.frontend.UndertowFrontendImpl;
import org.rcsb.idmapper.input.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TestIdMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestIdMapper.class);

    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_RSOCKET_PORT = 7000;
    public static final String TRANSLATE = "/translate";
    public static final String GROUP = "/group";
    public static final String ALL = "/all";
    public static final String MONGODB_URI = "MONGODB_URI";

    public static void main(String[] args) {

        var connectionString = Objects.requireNonNull(System.getenv(MONGODB_URI),
                String.format("The environment variable [ %s ] with Mongo database connection string (URI) must be set", MONGODB_URI));
        var backend = new BackendImpl(
                new DataProvider(connectionString){
                    @Override
                    public Closeable connect() {
                        return new Closeable() {
                            @Override
                            public void close() throws IOException {
//noop
                            }
                        };
                    }

                    @Override
                    public CompletableFuture<Void> initialize(Repository r) {
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void postInitializationCheck(Repository repository) throws Exception {
                        //noop;
                    }
                },
                new Repository(){
                    @Override
                    public Collection<String> lookup(String id, Input.Type from, Input.Type to, ContentType ct) {
                        return List.of("BHH4_1", "BHH4_2");
                    }
                }
        );

        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var mapper = new JsonMapper().create();
        var undertow = new UndertowFrontendImpl<>(backend, DEFAULT_HTTP_PORT, mapper);
        var rsocket = new RSocketFrontendImpl<>(backend, DEFAULT_RSOCKET_PORT, mapper);

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
