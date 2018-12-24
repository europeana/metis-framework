package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during media extraction.
 */
public class MediaExtractionException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -5753149269891298793L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   */
  public MediaExtractionException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The cause.
   */
  public MediaExtractionException(String message, Throwable cause) {
    super(message, cause);
  }
}
