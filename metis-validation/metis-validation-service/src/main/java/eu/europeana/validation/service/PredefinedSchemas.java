package eu.europeana.validation.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores list of all schemas defined by the application
 */
public class PredefinedSchemas {

    private Map<String, PredefinedSchema> schemas = new HashMap<>();

    /**
     * Inserts new schema to the list of all predefined schemas
     *
     * @param name                   name of new schema
     * @param location               place where schema files are stored
     * @param rootFileLocation       file name that should be used as a entry point for validation
     * @param schematronFileLocation file name that should be used as a entry point for schematron rules
     */
    public void add(String name, String location, String rootFileLocation,String schematronFileLocation) {
        PredefinedSchema predefinedSchema = new PredefinedSchema(name, location, rootFileLocation, schematronFileLocation);
        schemas.put(predefinedSchema.getKey(), predefinedSchema);
    }

    /**
     * Gets schema for given key
     *
     * @param key key that should be used for schema retrieval
     * @return found schema
     */
    public PredefinedSchema get(String key) {
        return schemas.get(key);
    }

    /**
     * Checks if schema exists for given key
     *
     * @param key key that should be used for check
     * @return whether the schema exists.
     */
    public boolean contains(String key) {
        return schemas.get(key) != null;
    }

    /**
     * Describes one schema used in application
     */
    public static class PredefinedSchema {
        private final String key;
        private final String location;
        private final String rootFileLocation;
        private final String schematronFileLocation;

        /**
         * Creates new schema
         *
         * @param key              schema key
         * @param location         location of schema files
         * @param rootFileLocation file name that should be used as a entry point for validation
         * @param schematronFileLocation file name that should be used as a entry point for schematron
         */
        public PredefinedSchema(String key, String location, String rootFileLocation, String schematronFileLocation) {
            this.key = key;
            this.location = location;
            this.rootFileLocation = rootFileLocation;
            this.schematronFileLocation = schematronFileLocation;
        }

        public String getKey() {
            return key;
        }

        public String getLocation() {
            return location;
        }

        public String getRootFileLocation() {
            return rootFileLocation;
        }

        public String getSchematronFileLocation() {
            return schematronFileLocation;
        }
    }
}
