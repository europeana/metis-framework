package eu.europeana.metis.mapping.exceptions;

/**
 * Exception thrown when the mapping CUD operations fail
 * Created by ymamakis on 6/16/16.
 */
public class SaveMappingFailedException extends Exception {
    /**
     * Defaut constructor
     * @param reason The reason why the mapping failed to persist or delete
     */
    public SaveMappingFailedException(String reason){
        super(String.format("Mapping failed. Reason: "+ reason));

    }
}
