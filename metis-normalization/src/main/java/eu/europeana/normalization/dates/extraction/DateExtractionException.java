package eu.europeana.normalization.dates.extraction;

/**
 * Exception for capturing errors during date extraction and instantiation.
 */
public class DateExtractionException extends Exception {

  private static final long serialVersionUID = -3332292346834265371L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public DateExtractionException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is
   * permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public DateExtractionException(String message, Throwable cause) {
    super(message, cause);
  }
}
