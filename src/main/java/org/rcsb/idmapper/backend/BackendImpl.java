package org.rcsb.idmapper.backend;

import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.common.constants.PdbIdFlavor;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.DataProviderConfig;
import org.rcsb.idmapper.backend.data.DataSource;
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
import java.util.List;
import java.util.Objects;

/**
 * This class is responsible for communication with upstream data provider
 * via {@link DataProvider} as well as storing data in the {@link Repository}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class BackendImpl {
    private final Logger logger = LoggerFactory.getLogger(BackendImpl.class);

    private final List<DataProviderConfig> dataProviders;
    private final Repository repository;
    private final PdbIdFlavor configuredDbFlavor;

    public BackendImpl(List<DataProviderConfig> dataProviders, Repository repository) {
        this(dataProviders, repository, PdbIdFlavor.LEGACY);
    }

    public BackendImpl(List<DataProviderConfig> dataProviders, Repository repository, PdbIdFlavor configuredDbFlavor) {
        this.dataProviders = dataProviders;
        this.repository = repository;
        this.configuredDbFlavor = configuredDbFlavor;
    }

    public void initialize() throws Exception {
        var start = Instant.now();
        logger.info("Initializing backend");
        for (var config : dataProviders) {
            try (@SuppressWarnings("unused") Closeable ignored =
                         config.dataProvider.connect(config.dataSource)) {
                if (config.dataSource == DataSource.CORE_PDB) {
                    validateCorePdbFlavor(config.dataProvider);
                }
                config.dataProvider.initialize(repository, config.dataSource).join();
            }
        }
        for (var config : dataProviders) {
            config.dataProvider.postInitializationCheck(repository, config.dataSource);
        }
        logger.info("Backend is initialized. Time took: [ {} ] minutes",
                Duration.between(start, Instant.now()).toMinutes());
    }

    private void validateCorePdbFlavor(DataProvider corePdbDataProvider) {
        PdbIdFlavor detectedFlavor = Objects.requireNonNullElse(corePdbDataProvider.detectCorePdbFlavor(), PdbIdFlavor.LEGACY);
        logger.info("Detected PDB ID flavor for CORE_PDB: {} (configured DB_FLAVOR={})", detectedFlavor, configuredDbFlavor);
        if (!configuredDbFlavor.equals(detectedFlavor)) {
            throw new IllegalStateException("PDB ID flavor mismatch for CORE_PDB: configured DB_FLAVOR=" +
                    configuredDbFlavor + " but detected=" + detectedFlavor + ". Application startup aborted.");
        }
    }

    public void start() {
        //TODO mark this one as ready to dispatch
    }

    //TODO replace with close and implement Closeable
    public void stop() {
        //TODO close connection etc
    }

    // builds a TranslateOutput by looking up mappings in the Repository. The method was
    // hit by /translate request ultimately.
    private Mono<TranslateOutput> dispatchTranslateInput(TranslateInput input) {
        var ids = input.ids != null ? input.ids :
                input.content_type
                        .stream()
                        .flatMap(ct -> repository.all(input.from, ct).stream())
                        .distinct()
                        .toList();
        var lookupIds = input.ids != null
                ? ids.stream().map(id -> Pair.of(id, IdFlavorSupport.normalizeTranslateInputId(id, input.from, configuredDbFlavor))).toList()
                : ids.stream().map(id -> Pair.of(id, id)).toList();

        return Flux.fromIterable(lookupIds)
                .flatMap(idPair -> Flux.fromIterable(input.content_type)
                        .map(ct -> Pair.of(idPair, ct)))
                .flatMap(p -> Flux.fromIterable(repository.lookup(p.getKey().getValue(), input.from, input.to, p.getValue()))
                                .map(toId -> Pair.of(p.getKey().getKey(), toId)))
                .reduce(new TranslateOutput(), (container, p1) -> {
                    container.results.put(p1.getValue(), p1.getKey());
                    return container;
                });

    }

    private Mono<TranslateOutput> dispatchGroupInput(GroupInput input) {
        return Flux.fromIterable(input.ids)
                .map(id -> Pair.of(id, IdFlavorSupport.normalizeGroupInputId(id, input.aggregation_method, configuredDbFlavor)))
                .flatMap(idPair -> Flux.fromIterable(repository.lookup(idPair.getValue(), input.aggregation_method, input.similarity_cutoff))
                        .map(gId -> Pair.of(idPair.getKey(), gId)))
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
