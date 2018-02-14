package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-30
 */
@ResponseStatus(value= HttpStatus.CONFLICT, reason="User Already Exists")
public class UserAlreadyExistsException extends GenericMetisException {
  private static final long serialVersionUID = -3332292346834265371L;
  public UserAlreadyExistsException(String message){
    super(message);
  }
}