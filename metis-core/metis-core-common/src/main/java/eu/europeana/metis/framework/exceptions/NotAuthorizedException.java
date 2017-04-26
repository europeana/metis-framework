package eu.europeana.metis.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by gmamakis on 8-2-17.
 */
@ResponseStatus(value= HttpStatus.UNAUTHORIZED, reason="Not authorized")
public class NotAuthorizedException extends Exception{
    public NotAuthorizedException(String id){
        super("API key "+id +" not authorized for writing");
    }
}
