package org.rcsb.idmapper.backend;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.lang3.tuple.Pair;
import org.rcsb.idmapper.backend.data.DataProvider;
import org.rcsb.idmapper.backend.data.Repository;
import org.rcsb.idmapper.frontend.input.AllInput;
import org.rcsb.idmapper.frontend.input.GroupInput;
import org.rcsb.idmapper.frontend.input.Input;
import org.rcsb.idmapper.frontend.input.TranslateInput;
import org.rcsb.idmapper.frontend.output.AllOutput;
import org.rcsb.idmapper.frontend.output.Output;
import org.rcsb.idmapper.frontend.output.TranslateOutput;

import java.io.Closeable;

/**
 * This class is responsible for communication with upstream data provider
 * via {@link DataProvider} as well as storing data in the {@link Repository}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class BackendImpl {

    private final DataProvider dataProvider;
    private final Repository repository;

    public BackendImpl(DataProvider dataProvider, Repository repository) {
        this.dataProvider = dataProvider;
        this.repository = repository;
    }

    public void initialize() throws Exception {
        //TODO fetch data from DataProvider into Repository
        try (Closeable closeable = dataProvider.connect()) {
            dataProvider.initialize(repository);
        }
    }

    public void start() {
        //TODO mark this one as ready to dispatch
    }

    //TODO replace with close and implement Closeable
    public void stop() {
        //TODO close connection etc
    }

    private Output dispatchTranslateInput(TranslateInput input) {
        return Observable.fromIterable(input.ids)
                .subscribeOn(Schedulers.computation())
                .flatMap(id -> Observable.fromIterable(input.content_type)
                        .map(ct -> Pair.of(id, ct)))
                .flatMap(p -> Observable.fromArray(repository.lookup(p.getKey(), input.from, input.to, p.getValue()))
                                .map(toId -> Pair.of(p.getKey(), toId)))
                .reduce(new TranslateOutput(), (container, p1) -> {
                    container.results.put(p1.getValue(), p1.getKey());
                    return container;
                })
                .blockingGet();
    }

    private Output dispatchGroupInput(GroupInput input) {
        return Observable.fromIterable(input.ids)
                .subscribeOn(Schedulers.computation())
                .flatMap(id -> Observable.fromArray(repository.lookup(id, input.aggregation_method, input.similarity_cutoff))
                        .map(gId -> Pair.of(id, gId)))
                .reduce(new TranslateOutput(), (container, p1) -> {
                    container.results.put(p1.getValue(), p1.getKey());
                    return container;
                })
                .blockingGet();
    }

    private Output dispatchAllInput(AllInput input) {
        return Observable.fromIterable(input.content_type)
                .subscribeOn(Schedulers.computation())
                .flatMap(ct -> Observable.fromArray(repository.all(input.from, ct)))
                .distinct()
                .reduce(new AllOutput(), (container, v) -> {
                    container.results.add(v);
                    return container;
                })
                .blockingGet();
    }

    public Output dispatch(Input input) {
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
