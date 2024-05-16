package org.rcsb.idmapper.frontend;

import com.google.gson.Gson;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.input.AllInput;
import org.rcsb.idmapper.input.GroupInput;
import org.rcsb.idmapper.input.Input;
import org.rcsb.idmapper.input.TranslateInput;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.rcsb.idmapper.IdMapperServer.*;

public class RSocketFrontendImpl<T extends FrontendContext<Payload>> implements Frontend {
    private RSocketServer server;
    private Disposable disposable;
    private final ServerTransport<CloseableChannel> transport;

    private final RSocket rSocket = new RSocket() {
        private Input extractInput(Payload payload) {
            try {
                switch (payload.getMetadataUtf8()) {
                    case TRANSLATE -> {
                        return mapper.fromJson(payload.getDataUtf8(), TranslateInput.class);
                    }
                    case GROUP -> {
                        return mapper.fromJson(payload.getDataUtf8(), GroupInput.class);
                    }
                    case ALL -> {
                        return mapper.fromJson(payload.getDataUtf8(), AllInput.class);
                    }
                }
                throw new IllegalArgumentException(String.format("Unknown request type: %s", payload.getDataUtf8()));
            } finally {
                // IMPORTANT: Received payloads must be explicitly released as soon as no longer needed by downstream
                // to prevent resource leak
                payload.release();
            }
        }
        @Override
        public Mono<Payload> requestResponse(final Payload incomingPayload) {
            return Mono.just(incomingPayload)
                    .map(this::extractInput)
                    .flatMap(backend::dispatch)
                    .map(output -> DefaultPayload.create(mapper.toJson(output)));//TODO maps chain may affect performance
        }
    };

    private final Gson mapper;
    private final BackendImpl backend;

    public RSocketFrontendImpl(BackendImpl backend, int port, Gson m) {
        this.transport = TcpServerTransport.create("0.0.0.0", port);
        this.backend = backend;
        this.mapper = m;
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
                .fragment(16777215) //TODO should match what we can send
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
