package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason="Bad Content")
public class BadContentException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;

  public BadContentException(String message) {
    super(message);
  }

  public BadContentException(String message, Throwable e) {
    super(message, e);
  }
}