package org.rcsb.idmapper.frontend;

//@Immutable
public class FrontendContext<T> {
    public final Input input;
    public final Output output;
    public final T supplements;

    private FrontendContext(Input input, T supplements) {
        this.input = input;
        this.supplements = supplements;
        this.output = null;
    }

    private FrontendContext(Input input, T supplements, Output output) {
        this.input = input;
        this.supplements = supplements;
        this.output = output;
    }

    public static <T> FrontendContext<T> create(Input input, T supplements){
        return new FrontendContext<>(input, supplements);
    }

    public FrontendContext<T> setOutput(Output output){
        return new FrontendContext<>(input, supplements, output);
    }
}
