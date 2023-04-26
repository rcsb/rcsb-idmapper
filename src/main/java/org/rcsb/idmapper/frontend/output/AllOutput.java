package org.rcsb.idmapper.frontend.output;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 4/21/23.
 *
 * @author Yana Rose
 */
public class AllOutput extends Output<List<String>> {

    public List<String> results = new ArrayList<>();

    @Override
    public List<String> getResults() {
        return results;
    }
}
