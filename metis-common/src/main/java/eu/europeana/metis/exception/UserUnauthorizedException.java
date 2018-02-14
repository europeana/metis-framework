package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-01
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Unauthorized")
public class UserUnauthorizedException extends GenericMetisException  {
  private static final long serialVersionUID = -3332292346834265371L;

  public UserUnauthorizedException(String message) {
    super(message);
  }

  public UserUnauthorizedException(String message, Throwable e) {
    super(message, e);
  }
}