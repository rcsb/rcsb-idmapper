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
    public static String[] expandArray(String[] array1, String[] array2) {
        String[] updated = new String[array1.length+array2.length];
        System.arraycopy(array1, 0, updated, 0, array1.length);
        System.arraycopy(array2, 0, updated, array1.length, array2.length);
        return updated;
    }

    public static <T> String[] collectionToStringArray(Collection<T> values) {
        String[] items = new String[values.size()];
        int i = 0;
        for (T v : values) {
            items[i] = String.valueOf(v);
            i++;
        }
        return items;
    }

    public static void addNonEmptyValues(Map<String, String[]> map, String key, Collection<String> values) {
        if (values == null || values.size() == 0)
            return;
        String[] existingValues = map.getOrDefault(key, new String[]{});
        String[] updatedValues = expandArray(existingValues, collectionToStringArray(values));
        map.put(key, updatedValues);
    }
}
