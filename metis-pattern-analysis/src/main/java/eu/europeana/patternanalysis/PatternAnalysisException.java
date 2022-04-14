package eu.europeana.patternanalysis;

/**
 * Exception used for a pattern analysis error.
 */
public class PatternAnalysisException extends Exception {

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is
   * permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public PatternAnalysisException(String message, Throwable cause) {
    super(message, cause);
  }

}
