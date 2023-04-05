package org.rcsb.idmapper;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.DataProvider;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.frontend.Frontend;
import org.rcsb.idmapper.frontend.FrontendContext;
import org.rcsb.idmapper.frontend.UndertowFrontendImpl;
import org.rcsb.idmapper.middleware.MiddlewareImpl;

public class IdMapper {
    public static void main(String[] args) {
        var backend = new BackendImpl(
                new DataProvider(),
                new Repository()
        );

        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var frontend = new UndertowFrontendImpl<>(8080);

        var middleware = new MiddlewareImpl();
        middleware.connect(frontend, backend);
        //TODO connect other frontends

        backend.initialize();
        frontend.initialize();
        //TODO initialize other frontends

        backend.start();
        var future = frontend.start();
        //TODO start other frontends

//        future.join();
        //TODO stop
    }
}
