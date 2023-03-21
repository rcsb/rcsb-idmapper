package org.rcsb.idmapper.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created on 3/14/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class AppUtils {

    /**
     * Concatenates two arrays
     *
     * @param array1 first array
     * @param array2 second array
     * @return resulting array contains data from {@param array1} and {@param array2}
     */
    private static String[] concatenateArrays(String[] array1, String[] array2) {
        String[] updated = new String[array1.length+array2.length];
        System.arraycopy(array1, 0, updated, 0, array1.length);
        System.arraycopy(array2, 0, updated, array1.length, array2.length);
        return updated;
    }

    public static void addNonEmptyValues(Map<String, String[]> map, String key, Collection<String> values) {
        if (values == null || values.size() == 0)
            return;
        String[] existingValues = map.getOrDefault(key, new String[]{});
        String[] updatedValues = concatenateArrays(existingValues, values.toArray(new String[0]));
        map.put(key, updatedValues);
    }
}
