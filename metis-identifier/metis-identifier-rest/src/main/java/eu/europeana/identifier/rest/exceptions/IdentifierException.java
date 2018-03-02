package eu.europeana.identifier.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 2/25/16.
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Identifier generation failed")
public class IdentifierException extends Exception {

    public IdentifierException(String message){
        super(message);
    }
}
