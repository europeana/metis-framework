package eu.europeana.metis.core.exceptions;

import eu.europeana.metis.exception.GenericMetisException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used if a plugin execution is not allowed.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-30
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "Plugin Execution Not Allowed")
public class PluginExecutionNotAllowed extends GenericMetisException {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public PluginExecutionNotAllowed(String message) {
    super(message);
  }
}
