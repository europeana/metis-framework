package eu.europeana.metis.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="User Not Found")
public class UserNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -3332292346834265371L;

  public UserNotFoundException(String name) {
    super("User not found: " + name);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(Throwable cause) {
    super(cause);
  }

}