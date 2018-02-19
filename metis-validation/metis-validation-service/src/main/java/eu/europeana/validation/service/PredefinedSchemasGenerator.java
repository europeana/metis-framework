package eu.europeana.validation.service;

import java.util.Properties;

/**
 * Creates PredefinedSchemas objects based on given properties
 * <p>
 * Created by pwozniak on 1/12/18
 */
public final class PredefinedSchemasGenerator {

    public static final String PREDEFINED_SCHEMAS_PREFIX = "predefinedSchemas.";

    private PredefinedSchemasGenerator() {
    }

    /**
     * Creates {@link PredefinedSchemas} instance based on provided properties
     *
     * @param properties properties to be used for {@link PredefinedSchemas} creation
     * @return the instance.
     */
    public static PredefinedSchemas generate(Properties properties) {
        PredefinedSchemas result = new PredefinedSchemas();
        String predefinedSchemas = properties.getProperty("predefinedSchemas");
        if (predefinedSchemas != null) {
            for (String predefinedSchema : predefinedSchemas.split(",")) {
                result.add(
                        predefinedSchema,
                        properties.getProperty(PREDEFINED_SCHEMAS_PREFIX + predefinedSchema + ".url"),
                        properties.getProperty(PREDEFINED_SCHEMAS_PREFIX + predefinedSchema + ".rootLocation"),
                        properties.getProperty(PREDEFINED_SCHEMAS_PREFIX + predefinedSchema + ".schematronLocation"));
            }
        }
        return result;
    }
}
