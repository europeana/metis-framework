package eu.europeana.validation.edm.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ValidationException extends Exception {

    private String message;
    private String id;

    public ValidationException(){
        super();
    }
    public ValidationException(String id, String message){
        super(message);
        this.message = message;
        this.id = id;
    }

    public String getId(){
        return this.id;
    }
}
