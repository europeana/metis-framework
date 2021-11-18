package eu.europeana.metis.core.exceptions;

import eu.europeana.metis.exception.GenericMetisException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used if index plugin is invalid
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "Invalid index plugin")
public class InvalidIndexPluginException extends GenericMetisException {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public InvalidIndexPluginException(String message) {
    super(message);
  }

}
