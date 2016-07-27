package eu.europeana.enrichment.harvester.transform.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that will read a ;-separated CSV file and will generate the mapping between a EDM/XML and a Contextual
 * class setter method
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public final class CsvUtils {

    /**
     * Method that reads a ;-separated CSV file and will generate the mapping between a EDM/XML and a Contextual class
     * setter method
     *
     * @param path The path the mapping file resides
     * @return A Map with the mappings between EDM/XML fields and setter methods;
     */
    public static final Map<String, String> readFile(String path) {
        Map<String, String> mappings = new HashMap<>();
        try {
            List<String> lines = FileUtils.readLines(new File(path));
            for (String str : lines) {
                String[] props = str.split(";");
                mappings.put(props[0], props[1]);
            }
            return mappings;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
