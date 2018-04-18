package eu.europeana.indexing.exception;

/**
 * Exception that may occur during configuration and setup of an indexer.
 * 
 * @author jochen
 *
 */
public class IndexerConfigurationException extends Exception {

  /** Required for implementations of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -8504829389742764531L;

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   */
  public IndexerConfigurationException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  public IndexerConfigurationException(String message, Exception cause) {
    super(message, cause);
  }
}
