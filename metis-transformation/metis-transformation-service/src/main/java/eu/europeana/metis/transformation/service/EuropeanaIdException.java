package eu.europeana.metis.transformation.service;

/**
 * Exception indicating that something went wrong extracting the provider ID from the source or
 * converting it to a Europeana ID.
 * 
 * @author jochen
 *
 */
public class EuropeanaIdException extends Exception {

  /** Implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -6596593096521279375L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   */
  public EuropeanaIdException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The causing exception.
   */
  public EuropeanaIdException(String message, Throwable cause) {
    super(message, cause);
  }
}
