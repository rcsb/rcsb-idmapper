package org.rcsb.idmapper;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.DataProvider;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.frontend.RSocketFrontendImpl;
import org.rcsb.idmapper.frontend.UndertowFrontendImpl;

import java.util.concurrent.CompletableFuture;

public class IdMapper {
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_RSOCKET_PORT = 7000;
    public static final String TRANSLATE = "/translate";
    public static final String GROUP = "/group";
    public static final String ALL = "/all";

    public static void main(String[] args) {
        var backend = new BackendImpl(
                new DataProvider(),
                new Repository()
        );

        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var undertow = new UndertowFrontendImpl<>(backend, DEFAULT_HTTP_PORT);
        var rsocket = new RSocketFrontendImpl<>(backend, DEFAULT_RSOCKET_PORT);

        backend.initialize();
        undertow.initialize();
        rsocket.initialize();
        //TODO initialize other frontends

        backend.start();
        CompletableFuture.allOf(
                undertow.start(),
                rsocket.start()
        ).join();
        //TODO stop
    }
}
