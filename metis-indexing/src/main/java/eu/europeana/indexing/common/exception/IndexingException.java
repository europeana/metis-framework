package eu.europeana.indexing.common.exception;

import java.io.Serial;

/**
 * Exception that may occur during indexing.
 * 
 * @author jochen
 *
 */
public abstract class IndexingException extends Exception {

  /** Required for implementations of {@link java.io.Serializable}. **/
  @Serial
  private static final long serialVersionUID = 2323679119224398983L;

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  protected IndexingException(String message, Exception cause) {
    super(message, cause);
  }

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   */
  protected IndexingException(String message) {
    super(message);
  }
}
