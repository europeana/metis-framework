package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used to encapsulate exceptions cause from an external resource
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-09
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad Content")
public class ExternalTaskException extends GenericMetisException {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public ExternalTaskException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and
   * cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the
   * {@link #getCause()} method).  (A <tt>null</tt> value is
   * permitted, and indicates that the cause is nonexistent or
   * unknown.)
   */
  public ExternalTaskException(String message, Throwable cause) {
    super(message, cause);
  }
}
