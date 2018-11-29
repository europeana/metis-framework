package eu.europeana.metis.mediaprocessing.exception;

/**
 * General class for exceptions generated in media topology.
 */
public class MediaException extends Exception {

  public final String reportError;
  public final boolean retry;

  public MediaException(String message, String reportError, Throwable cause, boolean retry) {
    super(message, cause);
    this.reportError = reportError;
    this.retry = retry;
  }

  public MediaException(String message, String reportError, Throwable cause) {
    this(message, reportError, cause, false);
  }

  public MediaException(String message, String reportError) {
    this(message, reportError, null, false);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   * #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   * (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public MediaException(String message, Throwable cause) {
    this(message, null, cause, false);
  }

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   * {@link #getMessage()} method.
   */
  public MediaException(String message) {
    this(message, null, null, false);
  }
}
