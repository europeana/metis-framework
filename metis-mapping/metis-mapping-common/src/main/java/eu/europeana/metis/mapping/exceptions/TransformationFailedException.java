package eu.europeana.metis.mapping.exceptions;

/**
 * Exception thrown when the transformation failed
 * Created by ymamakis on 6/9/16.
 */
public class TransformationFailedException extends Exception {

    /**
     * Default constructor
     * @param recordId The record identifier of the record
     * @param mapping The mapping used for the transformation
     */
    public TransformationFailedException(String recordId, String mapping){
        super (String.format("XSLT transformation failed for record %s using mapping %s", recordId, mapping));
    }
}
