package org.rcsb.idmapper.backend;

import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.input.AllInput;
import org.rcsb.idmapper.input.GroupInput;
import org.rcsb.idmapper.input.Input;
import org.rcsb.idmapper.input.TranslateInput;
import org.rcsb.idmapper.output.AllOutput;
import org.rcsb.idmapper.output.Output;
import org.rcsb.idmapper.output.TranslateOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;

/**
 * This class is responsible for communication with upstream data provider
 * via {@link DataProvider} as well as storing data in the {@link Repository}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class BackendImpl {
    private final Logger logger = LoggerFactory.getLogger(BackendImpl.class);

    private final DataProvider dataProvider;
    private final Repository repository;

    public BackendImpl(DataProvider dataProvider, Repository repository) {
        this.dataProvider = dataProvider;
        this.repository = repository;
    }

    public void initialize() throws Exception {
        try (Closeable closeable = dataProvider.connect()) {
            var start = Instant.now();
            logger.info("Initializing backend");
            dataProvider.initialize(repository)
                    .join();
            logger.info("Backend is initialized. Time took: [ {} ] minutes",
                    Duration.between(start, Instant.now()).toMinutes());
        }
    }

    public void start() {
        //TODO mark this one as ready to dispatch
    }

    //TODO replace with close and implement Closeable
    public void stop() {
        //TODO close connection etc
    }

    private Mono<TranslateOutput> dispatchTranslateInput(TranslateInput input) {
        return Flux.fromIterable(input.ids)
                .flatMap(id -> Flux.fromIterable(input.content_type)
                        .map(ct -> Pair.of(id, ct)))
                .flatMap(p -> Flux.fromIterable(repository.lookup(p.getKey(), input.from, input.to, p.getValue()))
                                .map(toId -> Pair.of(p.getKey(), toId)))
                .reduce(new TranslateOutput(), (container, p1) -> {
                    container.results.put(p1.getValue(), p1.getKey());
                    return container;
                });

    }

    private Mono<TranslateOutput> dispatchGroupInput(GroupInput input) {
        return Flux.fromIterable(input.ids)
                .flatMap(id -> Flux.fromIterable(repository.lookup(id, input.aggregation_method, input.similarity_cutoff))
                        .map(gId -> Pair.of(id, gId)))
                .reduce(new TranslateOutput(), (container, p1) -> {
                    container.results.put(p1.getValue(), p1.getKey());
                    return container;
                });
    }

    private Mono<AllOutput> dispatchAllInput(AllInput input) {
        return Flux.fromIterable(input.content_type)
                .flatMap(ct -> Flux.fromIterable(repository.all(input.from, ct)))
                .distinct()
                .reduce(new AllOutput(), (container, v) -> {
                    container.results.add(v);
                    return container;
                });
    }

    public Mono<? extends Output<?>> dispatch(Input input) {
        if (input instanceof TranslateInput t) {
            return dispatchTranslateInput(t);
        } else if (input instanceof GroupInput g) {
            return dispatchGroupInput(g);
        } else if (input instanceof AllInput a) {
            return dispatchAllInput(a);
        } else {
            throw new UnsupportedOperationException("Unsupported input "+input);
        }
    }
}
