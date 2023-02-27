package org.rcsb.idmapper.middleware;

import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.backend.Task;
import org.rcsb.idmapper.frontend.FrontendImpl;

/**
 * This class is responsible for establishing a connection between {@link FrontendImpl and {@link BackendImpl}}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class MiddlewareImpl {

    public <T extends Task<T>> void connect(FrontendImpl frontend, BackendImpl backend){
        //TODO listen frontend and dispatch to backend e.g.
        while(frontend.listen().hasNext())
            frontend.response(
                backend.dispatch(new Task<T>(frontend.listen().next())).output);
    }
}
