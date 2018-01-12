package eu.europeana.validation.service;

import java.util.HashMap;
import java.util.Map;

public class PredefinedSchemas {

    private Map<String, PredefinedSchema> predefinedSchemas = new HashMap<>();

    public void add(String name, String location, String rootFileLocation) {
        PredefinedSchema predefinedSchema = new PredefinedSchema(name, location, rootFileLocation);
        predefinedSchemas.put(predefinedSchema.getKey(), predefinedSchema);
    }

    public PredefinedSchema get(String key) {
            return predefinedSchemas.get(key);
    }

    public boolean contains(String key) {
        return predefinedSchemas.get(key) != null;
    }

    public class PredefinedSchema {
        private String key;
        private String location;
        private String rootFileLocation;

        public PredefinedSchema(String key, String location, String rootFileLocation) {
            this.key = key;
            this.location = location;
            this.rootFileLocation = rootFileLocation;
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
    }
}