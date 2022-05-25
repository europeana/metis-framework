package eu.europeana.enrichment.api.external.model;

// Request class for sending request to Entity API client
public class EnrichmentQuery {

    /**
     * value for enrichment. Could be text or URI
     */
    private String valueToEnrich;

    /**
     * Language for enrichment. Used for text Search
     */
    private String language;

    /**
     * Entity Type for enrichment.
     * Multiple value of entity types will be a comma seperated string.
     */
    private String type;

    /**
     * To identify if the valueToEnrich is text or URI
     */
    private boolean isReference;

    public EnrichmentQuery(String valueToEnrich, String language, String type, boolean isReference) {
        this.valueToEnrich = valueToEnrich;
        this.language = language;
        this.type = type;
        this.isReference = isReference;
    }

    public EnrichmentQuery(String valueToEnrich, String type, boolean isReference) {
        this.valueToEnrich = valueToEnrich;
        this.type = type;
        this.isReference = isReference;
    }

    public String getValueToEnrich() {
        return valueToEnrich;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValueToEnrich(String valueToEnrich) {
        this.valueToEnrich = valueToEnrich;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isReference() {
        return isReference;
    }

    public void setReference(boolean reference) {
        this.isReference = reference;
    }

    @Override
    public String toString() {
        return "EntityClientRequest{ valueToEnrich='" + valueToEnrich + '\'' + ", language='" + language + '\'' + ", type='" + type + '\'' + '}';
    }
}
