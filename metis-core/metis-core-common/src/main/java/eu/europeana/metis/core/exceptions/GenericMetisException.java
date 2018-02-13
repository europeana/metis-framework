package eu.europeana.metis.core.exceptions;

/**
 * Generic Metis exception that would encapsulate the actual exception.
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-13
 */
public class GenericMetisException extends Exception{

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public GenericMetisException(String message) {
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
  public GenericMetisException(String message, Throwable cause) {
    super(message, cause);
  }

}
