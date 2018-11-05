package eu.europeana.indexing.exception;

/**
 * This exception is one thrown by the indexer if there is a problem related to setting up the
 * indexer or setting up the connection to the target data storage.
 */
public class SetupRelatedIndexingException extends IndexingException {

  /** Required for implementations of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -3730418093071300920L;

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   */
  public SetupRelatedIndexingException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  public SetupRelatedIndexingException(String message, Exception cause) {
    super(message, cause);
  }
}
