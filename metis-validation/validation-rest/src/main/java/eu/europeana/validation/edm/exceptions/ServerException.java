package eu.europeana.validation.edm.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ServerException extends Exception{

    private String message;

    public ServerException(){
        super();
    }
    public ServerException(String message){
        super(message);
        this.message = message;
    }
}
