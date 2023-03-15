package org.rcsb.idmapper;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.Repository;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.frontend.FrontendImpl;
import org.rcsb.idmapper.middleware.MiddlewareImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdMapper {

    private static final Logger logger = LoggerFactory.getLogger(IdMapper.class);

    public static void main(String[] args) {
        var backend = new BackendImpl(
                new DataProvider(),
                new Repository()
        );
        //TODO there may be multiple frontends e.g. one for RSocket, another for Undertow. Hence a factory will be needed
        var frontend = new FrontendImpl();
        var middleware = new MiddlewareImpl();
        try {
            middleware.connect(frontend, backend);
            //TODO connect other frontends

            backend.initialize();
            frontend.initialize();
            //TODO initialize other frontends

            backend.start();
            frontend.start();
            //TODO start other frontends
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            //TODO stop
            backend.stop();
        }
    }
}
