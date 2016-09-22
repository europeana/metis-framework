package eu.europeana.metis.preview.service;

import java.util.Date;

/**
 * Collection Id utils
 * Created by ymamakis on 9/2/16.
 */
public class CollectionUtils {

    /**
     * Generate Collection Identifier
     * @return Generate timestamp-based collection identifier
     */
    public static String generateCollectionId(){
        return  Long.toString(new Date().getTime());
    }
}
