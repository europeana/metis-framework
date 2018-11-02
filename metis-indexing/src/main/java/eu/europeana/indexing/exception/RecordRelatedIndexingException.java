package eu.europeana.indexing.exception;

/**
 * This exception is one thrown by the indexer if there is a record related problem. This signifies
 * that the problem is likely not with the setup or the indexer, but rather with the individual
 * record that was submitted for indexing.
 */
public class RecordRelatedIndexingException extends IndexingException {

  /** Required for implementations of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -8887552792600523436L;

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   */
  public RecordRelatedIndexingException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  public RecordRelatedIndexingException(String message, Exception cause) {
    super(message, cause);
  }
}
