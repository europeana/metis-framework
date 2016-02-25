package eu.europeana.metis.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * No Organization was found either in METIS or in CRM
 * Created by ymamakis on 2/25/16.
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No organization found")
public class NoOrganizationExceptionFound extends Exception {

    public NoOrganizationExceptionFound(String message){
        super(message);
    }
}
