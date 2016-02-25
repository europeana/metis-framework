package eu.europeana.metis.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * No datasets were found
 * Created by ymamakis on 2/25/16.
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No dataset found")
public class NoDatasetFoundException extends Exception {
    public NoDatasetFoundException(String name){
        super("No dataset found with name: "+name);
    }
}
