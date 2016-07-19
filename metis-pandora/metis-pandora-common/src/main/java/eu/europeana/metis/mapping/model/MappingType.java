package eu.europeana.metis.mapping.model;

/**
 * The mapping type
 * Created by gmamakis on 8-4-16.
 */
public enum MappingType {
    /**
     * Constant value
     */
    CONSTANT,
    /**
     * XPath mapping
     */
    XPATH,
    /**
     * From a variable
     */
    PARAMETER,
    /**
     * No mapping
     */
    EMPTY;
}
