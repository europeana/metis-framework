package eu.europeana.metis.core.exceptions;

import eu.europeana.metis.exception.GenericMetisException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used if a workflow execution does not exist in the database.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-31
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No userWorkflowExecution found")
public class NoWorkflowExecutionFoundException extends GenericMetisException {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public NoWorkflowExecutionFoundException(String message) {
    super(message);
  }
}
