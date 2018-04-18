package eu.europeana.indexing.exception;

/**
 * Exception that may occur during indexing.
 * 
 * @author jochen
 *
 */
public class IndexingException extends Exception {

  /** Required for implementations of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 2323679119224398983L;

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  public IndexingException(String message, Exception cause) {
    super(message, cause);
  }

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   */
  public IndexingException(String message) {
    super(message);
  }
}
