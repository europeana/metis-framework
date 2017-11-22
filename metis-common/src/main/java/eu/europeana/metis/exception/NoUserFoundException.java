package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No user found")
public class NoUserFoundException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;
  public NoUserFoundException(String message){
    super(message);
  }
}


