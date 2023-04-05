package org.rcsb.idmapper.middleware;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.rcsb.idmapper.IdMapper;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.frontend.Frontend;
import org.rcsb.idmapper.frontend.FrontendContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for establishing a connection between {@link Frontend and {@link BackendImpl}}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class MiddlewareImpl implements AutoCloseable {

    private Disposable disposable;

    public <T extends Frontend<V>, V extends FrontendContext<Z>, Z> void connect(T frontend, BackendImpl backend){
        disposable = frontend.observe()
                //TODO cannot joggle with threads here, as exchange is not thread safe, with Undertow this pipeline is executed on XNIO thread due to BlockingHttpHanler
//                .observeOn(Schedulers.computation())
                .map(frontendContext -> {
                    var output = backend.dispatch(frontendContext.input);

                    return frontendContext.setOutput(output);
                })
//                .observeOn(Schedulers.io())
                .subscribe(frontendContext -> {
                    frontend.sendResponse((V) frontendContext);//TODO again how to avoid cast here?!
                });
    }

    @Override
    public void close() {
        disposable.dispose();
    }
}
