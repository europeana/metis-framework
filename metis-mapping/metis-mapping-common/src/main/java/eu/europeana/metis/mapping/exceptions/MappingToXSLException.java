package eu.europeana.metis.mapping.exceptions;

/**
 * Exception thrown when the mapping fails to serialize to XSL
 * Created by ymamakis on 6/9/16.
 */
public class MappingToXSLException extends Exception {
    /**
     * Default constructor
     * @param dataset The dataset for which the XSL generation failed
     * @param mappingName The mapping name
     * @param reason The reason (underlying exception) why it failed
     */
    public MappingToXSLException(String dataset, String mappingName, String reason){
        super(String.format("Mapping %s conversion to XSLT for dataset %s. Reason: %s", mappingName,dataset,reason));
    }
}
