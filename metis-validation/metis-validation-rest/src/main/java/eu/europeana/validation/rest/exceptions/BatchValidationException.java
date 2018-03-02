package eu.europeana.validation.rest.exceptions;

import eu.europeana.validation.model.ValidationResultList;


/**
 * Created by ymamakis on 2/24/16.
 */
public class BatchValidationException extends Exception {

    private final ValidationResultList list;

    /**
     * Cretes exception instance based on
     *
     * @param message description of the problem
     * @param list    list of results provided by validation engine
     */
    public BatchValidationException(String message, ValidationResultList list) {
        super(message);
        this.list = list;
    }

    public ValidationResultList getList() {
        return list;
    }

}
