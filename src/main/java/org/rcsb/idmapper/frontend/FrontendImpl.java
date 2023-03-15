package org.rcsb.idmapper.frontend;

import java.util.Iterator;

/**
 * Main responsibility - listen for incoming requests and form {@link org.rcsb.idmapper.backend.Task}
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class FrontendImpl{

    public void initialize(){
        //TODO initialize underlying framework: RSocket; Undertow; Micronaut
    }

    public void start(){
        //TODO start process
    }

    //TODO replace with close and implement Closeable
    public void stop(){
        //TODO clean resources; stop processes
    }

    public void response(Output output){
        //TODO send response
    }

    public Iterator<Input> listen(){

        //TODO not sure about iterator here, probably should be blocking queue or something. Iterator is good enough for demo
        return new Iterator<Input>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Input next() {
                return null;
            }
        };
    }
}
