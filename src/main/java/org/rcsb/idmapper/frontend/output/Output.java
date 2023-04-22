package org.rcsb.idmapper.frontend.output;

/**
 * Represents output provided to downstream client e.g. Arches
 *
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public interface Output<T> {
    T getResults();
}
