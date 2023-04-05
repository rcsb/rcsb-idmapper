package org.rcsb.idmapper.frontend;

import io.reactivex.rxjava3.core.Observable;
import org.rcsb.idmapper.middleware.Task;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Main responsibility - listen for incoming requests and form {@link Task}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public interface Frontend<T extends FrontendContext<?>> extends Closeable {

    public void initialize();

    public CompletableFuture<Void> start();

    public void sendResponse(T context);

    public Observable<T> observe();
}
