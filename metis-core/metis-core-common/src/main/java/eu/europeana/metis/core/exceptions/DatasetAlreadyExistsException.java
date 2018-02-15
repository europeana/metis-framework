package eu.europeana.metis.core.exceptions;

import eu.europeana.metis.exception.GenericMetisException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used if a dataset already exists in the database.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-17
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Dataset already exists")
public class DatasetAlreadyExistsException extends GenericMetisException {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public DatasetAlreadyExistsException(String message) {
    super(message);
  }
}
