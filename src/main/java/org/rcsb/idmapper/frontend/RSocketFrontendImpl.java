package org.rcsb.idmapper.frontend;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.rcsb.idmapper.backend.BackendImpl;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.rcsb.idmapper.IdMapper.*;

public class RSocketFrontendImpl<T extends FrontendContext<Payload>> implements Frontend {
    private final int port;
    private RSocketServer server;
    private Disposable disposable;
    private final ServerTransport<CloseableChannel> transport;
    private RSocket rSocket = new RSocket() {
        private Input extractInput(Payload payload){
            var gson = new Gson();
            switch (payload.getMetadataUtf8()){
                case TRANSLATE:
                    return gson.fromJson(payload.getDataUtf8(), TranslateInput.class);
                case GROUP:
                    return gson.fromJson(payload.getDataUtf8(), GroupInput.class);
                case ALL:
                    return gson.fromJson(payload.getDataUtf8(), AllInput.class);
            }
            throw new IllegalArgumentException(String.format("Unknown command: %s", payload.getDataUtf8()));
        }
        @Override
        public Mono<Payload> requestResponse(final Payload incoming) {
            return Mono.just(incoming)
                    .map(this::extractInput)
                    .map(backend::dispatch)
                    .map(output -> DefaultPayload.create(new Gson().toJson(output)));//TODO maps chain may affect performance
        }
    };

    private final BackendImpl backend;

    public RSocketFrontendImpl(BackendImpl backend, int port) {
        this.port = port;
        this.transport = TcpServerTransport.create("0.0.0.0", port);
        this.backend = backend;
    }

    @Override
    public void initialize() {
        this.server = RSocketServer.create(
                (setup, rsocket) -> {
                    return Mono.just(rSocket);//returned to the client
                })
                .payloadDecoder(PayloadDecoder.ZERO_COPY);

    }

    @Override
    public CompletableFuture<Void> start() {
        var future = new CompletableFuture<Void>();
        this.disposable = this.server
                .bindNow(transport)
                .onClose()
                .subscribe(null, future::completeExceptionally, () -> future.complete(null));
        return future;
    }

    @Override
    public void close() throws IOException {
        disposable.dispose();
    }
}