package eu.europeana.normalization.util;

/**
 * Exception indicating that something went wrong during normalization.
 * 
 * @author jochen
 *
 */
public class NormalizationException extends Exception {

  /** Required for instances of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 2298940685190477391L;

  /**
   * Constructor.
   * 
   * @param message The message of the exception. Can be null.
   * @param cause The cause of the exception. Can be null.
   */
  public NormalizationException(String message, Exception cause) {
    super(message, cause);
  }
}
