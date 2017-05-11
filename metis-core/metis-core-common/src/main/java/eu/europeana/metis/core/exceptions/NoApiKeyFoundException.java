package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by gmamakis on 7-2-17.
 */

    @ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No API key found")
    public class NoApiKeyFoundException extends Exception {
        public NoApiKeyFoundException(String id){
            super("No API key found with name: "+id);
        }
    }

