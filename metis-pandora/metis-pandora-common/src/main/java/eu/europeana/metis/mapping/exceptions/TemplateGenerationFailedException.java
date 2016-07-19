package eu.europeana.metis.mapping.exceptions;

/**
 * Exception thrown when an XSD fails to generate a template
 * Created by ymamakis on 6/13/16.
 */
public class TemplateGenerationFailedException extends Exception {
    /**
     * Default constructor
     * @param name The name of the mapping
     * @param file The file pointing to the XSD
     * @param reason The reason why the exception was thrown
     */
    public TemplateGenerationFailedException(String name, String file, String reason){
        super(String.format("Template generation failed for %s from file %s. Reason: %s",name,file,reason));
    }
}
