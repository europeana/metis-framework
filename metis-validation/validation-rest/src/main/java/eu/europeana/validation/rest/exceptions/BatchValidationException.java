package eu.europeana.validation.rest.exceptions;

import eu.europeana.validation.model.ValidationResultList;


/**
 * Created by ymamakis on 2/24/16.
 */
public class BatchValidationException extends Exception {

    private String message;

    private ValidationResultList list;

    public BatchValidationException(String message,ValidationResultList list){
        super(message);
        this.message = message;
        this.list = list;
    }

    public ValidationResultList getList(){
        return list;
    }

}
