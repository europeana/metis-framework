package eu.europeana.metis.zoho;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for describing zoho type of exceptions.
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "Bad Content")
public class ZohoException extends Exception {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   * {@link #getMessage()} method.
   */
  public ZohoException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   * #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   * (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public ZohoException(String message, Throwable cause) {
    super(message, cause);
  }
}
