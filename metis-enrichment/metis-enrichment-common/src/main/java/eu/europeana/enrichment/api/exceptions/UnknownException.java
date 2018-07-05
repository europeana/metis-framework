package eu.europeana.enrichment.api.exceptions;

/**
 * Generic Exception not covered by the standard Jackson Exceptions
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class UnknownException extends RuntimeException {
  private static final long serialVersionUID = -7418853794840766813L;

  /**
   * A generic non-standard Exception
   *
   * @param message The message caused by the original exception
   */
  public UnknownException(String message) {
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
  public UnknownException(String message, Throwable cause) {
    super(message, cause);
  }
}
