package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceptions used if a user was not found in the database.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="User Not Found")
public class UserNotFoundException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;

  public UserNotFoundException(String name) {
    super("User not found: " + name);
  }
}
