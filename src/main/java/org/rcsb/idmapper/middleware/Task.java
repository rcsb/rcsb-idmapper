package org.rcsb.idmapper.middleware;

import org.rcsb.idmapper.frontend.FrontendContext;
import org.rcsb.idmapper.frontend.Input;
import org.rcsb.idmapper.frontend.Output;

/**
 * Performs whatever actions required to transform {@link Input} into {@link Output}
 *
 * Basically a DTO (Data Transfer Object)
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class Task {
    public final Input input;
    public Output output;
    public final FrontendContext<?> context;

    public Task(Input input, FrontendContext<?> context){
        this.input = input;
        this.context = context;
    }

}
