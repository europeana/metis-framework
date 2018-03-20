package eu.europeana.metis.dereference.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 2/25/16.
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Dereferencing failed")
public class DereferenceException extends RuntimeException {

	/** Required for implementations of {@link java.io.Serializable} **/
	private static final long serialVersionUID = 2510936695311769525L;

	public DereferenceException(String message, String uri) {
        super("Dereferencing failed for uri: "+ uri +" with root cause: " + message);
    }
}
