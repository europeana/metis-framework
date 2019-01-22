package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during setting up one of the worker objects in
 * the media processing library.
 */
public class MediaProcessorException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 8090383001647258984L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   */
  public MediaProcessorException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The cause.
   */
  public MediaProcessorException(String message, Exception cause) {
    super(message, cause);
  }
}
