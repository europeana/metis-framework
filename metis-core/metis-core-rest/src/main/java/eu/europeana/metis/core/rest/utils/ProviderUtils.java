package eu.europeana.metis.core.rest.utils;

import eu.europeana.metis.core.common.Role;

/**
 * Created by gmamakis on 3-2-17.
 */
public class ProviderUtils {

    public static Role getRoleFromString(String s){
        switch (s) {
            case "data_aggregator":
                return Role.DATA_AGGREGATOR;
            case "direct_provider":
                return Role.DIRECT_PROVIDER;
            case "content_provider":
                return Role.CONTENT_PROVIDER;
            case "europeana":
                return Role.EUROPEANA;
        }
        return null;
    }
}
