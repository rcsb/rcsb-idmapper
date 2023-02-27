package org.rcsb.idmapper.backend;


import java.util.concurrent.Callable;

/**
 * This class is responsible for communication with upstream data provider via {@link DataProvider} as well as storing
 * data in the {@link Repository}
 *
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class BackendImpl {
    public BackendImpl(DataProvider dataProvider, Repository repository){

    }

    public void initialize(){
        //TODO fetch data from DataProvider into Repository
    }

    public void start(){
        //TODO mark this one as ready to dispatch
    }

    //TODO replace with close and implement Closeable
    public void stop(){
        //TODO close connection etc
    }

    public Task dispatch(Task task){
        //TODO dispatch and perform task
        return task;
    }

}
