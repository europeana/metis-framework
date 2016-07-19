package eu.europeana.metis.mapping.exceptions;

/**
 * Mapping not found exception. Response code 404
 * Created by ymamakis on 6/16/16.
 */
public class MappingNotFoundException extends Exception {

    /**
     * Default constructor
     * @param mappingId The id requested
     */
    public MappingNotFoundException(String mappingId){
        super(String.format("Mapping with id: %s Not Found. Reason: ", mappingId));

    }
}
