package eu.europeana.validation.service;

import java.util.Properties;

/**
 * Creates PredefinedSchemas objects based on given properties
 *
 * Created by pwozniak on 1/12/18
 */
public class PredefinedSchemasGenerator {

    private PredefinedSchemasGenerator(){}

    public static PredefinedSchemas generate(Properties properties){
        PredefinedSchemas result = new PredefinedSchemas();
        String predefinedSchemas = properties.getProperty("predefinedSchemas");
        if(predefinedSchemas!=null){
            for (String predefinedSchema : predefinedSchemas.split(",")) {
                result.add(predefinedSchema, properties.getProperty("predefinedSchemas." + predefinedSchema + ".url"), properties.getProperty("predefinedSchemas." + predefinedSchema + ".rootLocation"));
            }
        }
        return result;
    }
}
