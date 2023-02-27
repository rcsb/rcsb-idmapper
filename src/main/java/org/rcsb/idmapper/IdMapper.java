package org.rcsb.idmapper;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.DataProvider;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.frontend.FrontendImpl;
import org.rcsb.idmapper.middleware.MiddlewareImpl;

public class IdMapper {
    public static void main(String[] args) {
        var backend = new BackendImpl(
                new DataProvider(),
                new Repository()
        );

        var frontend = new FrontendImpl();
        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed

        var middleware = new MiddlewareImpl();
        middleware.connect(frontend, backend);
        //TODO connect other frontends

        backend.initialize();
        frontend.initialize();
        //TODO initialize other frontends

        backend.start();
        frontend.start();
        //TODO start other frontends

        //TODO stop
    }
}
