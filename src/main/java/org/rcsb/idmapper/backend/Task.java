package org.rcsb.idmapper.backend;

import org.rcsb.idmapper.frontend.Input;
import org.rcsb.idmapper.frontend.Output;

import java.util.concurrent.Callable;

/**
 * Performs whatever actions required to transform {@link Input} into {@link Output}
 *
 * Basically a DTO (Data Transfer Object)
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class Task<T extends Task<T>> implements Callable<T> {
    public Input input;
    public Output output;

    public Task(Input input){

    }

    //TODO perform task; maybe done via injecting logic
    public T call() throws Exception {
        //TODO do stuff
        return (T) this;
    }
}
