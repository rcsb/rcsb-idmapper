package org.rcsb.idmapper.frontend;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.rcsb.idmapper.IdMapper;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class UndertowFrontendImpl<T extends FrontendContext<HttpServerExchange>> implements Frontend<T> {
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain; charset=utf-8";
    private final AttachmentKey<T> contextAttachmentKey = AttachmentKey.create(FrontendContext.class);
    /**
     * Effectively glues Frontend with Backend in Middleware, see {@link org.rcsb.idmapper.middleware.MiddlewareImpl}
     */
    private final Subject<T> subject = PublishSubject.create();
    private final int port;
    private Undertow server;

    private final HttpHandler rootHandler = new RoutingHandler()
            .get("/", new IamOkHandler())
            .post(IdMapper.TRANSLATE, new BlockingHandler(//effectively offloads to XNIO thread, hence thread per request model :(
                    new ExtractJson<>(TranslateInput.class,
                            new TaskEmitterHandler())))
            .post(IdMapper.GROUP, new BlockingHandler(
                    new ExtractJson<>(GroupInput.class,
                            new TaskEmitterHandler())))
            .post(IdMapper.ALL, new BlockingHandler(
                    new ExtractJson<>(AllInput.class,
                            new TaskEmitterHandler())));


    public UndertowFrontendImpl(int port) {
        this.port = port;
    }

    public void initialize() {
        this.server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0", rootHandler)
                .build();
    }

    public CompletableFuture<Void> start() {
        //TODO guava preconditions
        //off load server from main thread
        return CompletableFuture.supplyAsync(() -> {
            server.start();
            return null;
        }, Executors.newSingleThreadExecutor());
    }

    public void close() {
        server.stop();
        subject.onComplete();
    }

    //TODO should be encapsulated into HttpHandler?
    public void sendResponse(T context) {
        var exchange = context.supplements;
        if(exchange.isResponseComplete()) throw new IllegalStateException("Response must not be completed at this stage");
        exchange
                .getResponseHeaders()
                .add(Headers.CONTENT_TYPE, APPLICATION_JSON);

        try (var writer = new OutputStreamWriter(
                new BufferedOutputStream(exchange.getOutputStream()))) {

            var mapper = new Gson();

            mapper.toJson(context.output, writer);
        }
        catch (IOException exception){
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
            exchange.getResponseSender().send(String.format("Internal server error: %s", exception.getMessage()));
        }
    }

    public Observable<T> observe() {
        return Observable.wrap(subject);
    }

    public static void main(String[] args) {
        var front = new UndertowFrontendImpl(8080);
        front.initialize();
        front.start();
    }

    private class IamOkHandler implements HttpHandler {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.getResponseHeaders()
                    .add(Headers.CONTENT_TYPE, TEXT_PLAIN);
            exchange.getResponseSender()
                    .send("IamOK");
        }
    }

    private class ExtractJson<V extends Input> implements HttpHandler {
        private final HttpHandler next;
        private final Class<V> clazz;

        private ExtractJson(Class<V> clazz, HttpHandler next) {
            this.next = next;
            this.clazz = clazz;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            var blocking = exchange.startBlocking();

            var mapper = new Gson();
            //TODO reactive or async to avoid blocking call here
            var input = mapper.fromJson(
                    new InputStreamReader(
                            new BufferedInputStream(blocking.getInputStream())), clazz);//will simply return 500 if json is invalid

            var context = FrontendContext.create(input, exchange);

            exchange.putAttachment(contextAttachmentKey, (T) context);//TODO without casting compiler ain't happy, can we do anything about it?!
            next.handleRequest(exchange);
        }
    }

    private class TaskEmitterHandler implements HttpHandler {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            var context = exchange.getAttachment(contextAttachmentKey);
            subject.onNext(context);
            //TODO do we want downstream subject here?
            /**
             * <java>
             *     downstream.subscribe(context -> {
             *          UndertowFrontendImpl.this.sendResponse(context)
             *       }
             *     )
             * </java>
             */

        }
    }
}
