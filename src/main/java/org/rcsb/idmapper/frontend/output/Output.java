package org.rcsb.idmapper.frontend.output;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents output provided to downstream client e.g. Arches
 *
 *
 * @since 27 Feb 2023
 * @author ingvord
 */
public class Output {
    public Multimap<String, String> results = ArrayListMultimap.create();
}
