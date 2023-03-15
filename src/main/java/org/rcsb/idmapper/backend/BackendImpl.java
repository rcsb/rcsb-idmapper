package org.rcsb.idmapper.backend;


import org.rcsb.idmapper.backend.data.DataProvider;

/**
 * This class is responsible for communication with upstream data provider via {@link DataProvider} as well as storing
 * data in the {@link Repository}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class BackendImpl {

    private final DataProvider dataProvider;
    private final Repository repository;

    public BackendImpl(DataProvider dataProvider, Repository repository){
        this.dataProvider = dataProvider;
        this.repository = repository;
    }

    public void initialize() {
        //TODO fetch data from DataProvider into Repository
        dataProvider.connect();
        dataProvider.initialize(repository);
        // we can close DB connection as soon as maps are initialized
        dataProvider.close();
    }

    public void start() {
        //TODO mark this one as ready to dispatch
    }

    //TODO replace with close and implement Closeable
    public void stop() {
        //TODO close connection etc
    }

    public Task dispatch(Task task){
        //TODO dispatch and perform task
        return task;
    }
}
