/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.enrichment.harvester.transform.util;

import java.util.*;
import java.util.Map.Entry;

/**
 * Utilities for Map and String[] normalization
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class NormalizeUtils {

    /**
     * Removes duplicate values from Map entries
     * @param map
     * @return 
     */
    public static Map<String, List<String>> normalizeMap(Map<String, List<String>> map) {
        if (map != null) {
            for (Entry<String, List<String>> entry : map.entrySet()) {
                List<String> list = entry.getValue();
                Set<String> set = new HashSet<>(list);
                entry.setValue(new ArrayList<>(set));
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * REmoves duplicate values from a String Array
     * @param array
     * @return 
     */
    public static String[] normalizeArray(String[] array) {
        if (array != null) {
            Set<String> set = new HashSet<>();
            for (String str : array) {
                set.add(str);
               
            }
             List<String> list = new ArrayList<>(set);
             return list.toArray(new String[list.size()]);
        }
        return null;
    }
}
