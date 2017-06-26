package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-22
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Empty API key")
public class EmptyApiKeyException extends Exception {

  private static final long serialVersionUID = -3332292346834265371L;

  public EmptyApiKeyException() {
    super("Cannot have an emtpy apiKey");
  }
}
