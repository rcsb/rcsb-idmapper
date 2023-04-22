package org.rcsb.idmapper.frontend.output;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Created on 4/21/23.
 *
 * @author Yana Rose
 */
public class TranslateOutput implements Output<Multimap<String, String>> {

    public Multimap<String, String> results = ArrayListMultimap.create();

    @Override
    public Multimap<String, String> getResults() {
        return results;
    }
}
