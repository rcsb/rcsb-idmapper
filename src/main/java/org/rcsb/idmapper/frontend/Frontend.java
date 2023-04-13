package org.rcsb.idmapper.frontend;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Main responsibility - listen for incoming requests and dispatch to {@link org.rcsb.idmapper.backend.BackendImpl}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public interface Frontend extends Closeable {

    public void initialize();

    public CompletableFuture<Void> start();
}
