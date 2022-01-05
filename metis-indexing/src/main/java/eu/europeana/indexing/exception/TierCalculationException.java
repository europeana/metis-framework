package eu.europeana.indexing.exception;

/**
 * Exception that may occur during indexing.
 *
 * @author jochen
 */
public class TierCalculationException extends RuntimeException {

  /**
   * Required for implementations of {@link java.io.Serializable}.
   **/
  private static final long serialVersionUID = 6021595467219232390L;

  /**
   * Constructor.
   *
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  public TierCalculationException(String message, Exception cause) {
    super(message, cause);
  }

  /**
   * Constructor.
   *
   * @param message The message. Can be null.
   */
  public TierCalculationException(String message) {
    super(message);
  }
}
