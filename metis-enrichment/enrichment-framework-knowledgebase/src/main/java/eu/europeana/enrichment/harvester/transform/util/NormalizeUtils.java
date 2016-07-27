/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
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
