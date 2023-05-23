package org.rcsb.idmapper.frontend;

import com.google.gson.Gson;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import org.rcsb.idmapper.IdMapper;
import org.rcsb.idmapper.backend.BackendImpl;
import org.rcsb.idmapper.input.AllInput;
import org.rcsb.idmapper.input.GroupInput;
import org.rcsb.idmapper.input.Input;
import org.rcsb.idmapper.input.TranslateInput;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class UndertowFrontendImpl<T extends FrontendContext<HttpServerExchange>> implements Frontend {
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain; charset=utf-8";
    private final AttachmentKey<T> contextAttachmentKey = AttachmentKey.create(FrontendContext.class);
    private final BackendImpl backend;
    private final int port;
    private Undertow server;
    private final Gson mapper;

    private final HttpHandler rootHandler = new RoutingHandler()
            .get("/", new IamOkHandler())
            .post(IdMapper.TRANSLATE, new BlockingHandler(//effectively offloads to XNIO thread, hence thread per request model :(
                    new ExtractJson<>(TranslateInput.class,
                            new TaskDispatcherHandler(new SendResponseHandler()))))
            .post(IdMapper.GROUP, new BlockingHandler(
                    new ExtractJson<>(GroupInput.class,
                            new TaskDispatcherHandler(new SendResponseHandler()))))
            .post(IdMapper.ALL, new BlockingHandler(
                    new ExtractJson<>(AllInput.class,
                            new TaskDispatcherHandler(new SendResponseHandler()))));


    public UndertowFrontendImpl(BackendImpl backend, int port, Gson m) {
        this.backend = backend;
        this.port = port;
        this.mapper = m;
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

            //TODO reactive or async to avoid blocking call here
            var input = mapper.fromJson(
                    new InputStreamReader(
                            new BufferedInputStream(blocking.getInputStream())), clazz);//will simply return 500 if json is invalid

            var context = FrontendContext.create(input, exchange);

            exchange.putAttachment(contextAttachmentKey, (T) context);//TODO without casting compiler ain't happy, can we do anything about it?!
            next.handleRequest(exchange);
        }
    }

    private class TaskDispatcherHandler implements HttpHandler {
        private final HttpHandler next;

        private TaskDispatcherHandler(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            var context = exchange.getAttachment(contextAttachmentKey);
            var output = backend.dispatch(context.input);

            exchange.putAttachment(contextAttachmentKey, (T)context.setOutput(output));
            next.handleRequest(exchange);
        }
    }

    private class SendResponseHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            if(exchange.isResponseComplete()) throw new IllegalStateException("Response must not be completed at this stage");
            exchange
                    .getResponseHeaders()
                    .add(Headers.CONTENT_TYPE, APPLICATION_JSON);

            try (var writer = new OutputStreamWriter(
                    new BufferedOutputStream(exchange.getOutputStream()))) {
                var context = exchange.getAttachment(contextAttachmentKey);
                mapper.toJson(context.output, writer);
            }
        }
    }
}
