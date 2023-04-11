package org.rcsb.idmapper.frontend;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.BackpressureStrategy;
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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.rcsb.idmapper.IdMapper.*;

public class RSocketFrontendImpl<T extends FrontendContext<Payload>> implements Frontend<T> {
    private final PublishSubject<T> upstream = PublishSubject.create();
    private final PublishSubject<T> downstream = PublishSubject.create();
    private final int port;
    private RSocketServer server;
    private Disposable disposable;
    private final ServerTransport<CloseableChannel> transport;
    private RSocket rSocket = new RSocket() {
        @Override
        public Mono<Payload> requestResponse(final Payload incoming) {
            return Flux.<Payload>create(sink -> {
                var d = handleRequestResponse(incoming)
                        .doOnNext(System.out::println)
                        .subscribe(outcoming -> {
                            sink.next(outcoming);
                            sink.complete();
                        });
                sink.onDispose(d::dispose);
            }).timeout(Duration.ofSeconds(3L))
              .singleOrEmpty();
        }
    };;

    public RSocketFrontendImpl(int port) {
        this.port = port;
        this.transport = TcpServerTransport.create("0.0.0.0", port);
    }

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

    private Observable<Payload> handleRequestResponse(Payload incoming){
        var input = extractInput(incoming);

        var context = FrontendContext.create(input, (Payload)null);

        upstream.onNext((T)context);//off loads to computational

        return Observable.wrap(downstream)
                .map(innerContext -> innerContext.supplements);
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
    public void sendResponse(T context) {
        downstream.onNext((T)FrontendContext.create(null, DefaultPayload.create(new Gson().toJson(context.output))));
    }

    @Override
    public Observable<T> observe() {
        return upstream.observeOn(Schedulers.computation());//observe on Computational
    }

    @Override
    public void close() throws IOException {
        disposable.dispose();
    }

    public static void main(String[] args) {
        var frontend = new RSocketFrontendImpl<>(7000);
        frontend.initialize();
        frontend.start().join();

    }
}
