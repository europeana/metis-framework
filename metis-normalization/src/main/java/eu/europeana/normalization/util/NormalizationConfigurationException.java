package eu.europeana.normalization.util;

/**
 * Exception occurring when setting up and configuring the normalization.
 * 
 * @author jochen
 *
 */
public class NormalizationConfigurationException extends Exception {

  /** Required for instances of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 5469123498931535061L;

  /**
   * Constructor.
   * 
   * @param message The exception message, can be null.
   * @param cause The cause of the exception, can be null.
   */
  public NormalizationConfigurationException(String message, Exception cause) {
    super(message, cause);
  }
}
