package eu.europeana.indexing.exception;

/**
 * This exception is one thrown by the indexer if there is a problem related with saving/indexing a
 * given record. This signifies that the problem is likely not with the local setup or the
 * individual record submitted for indexing, but rather with the connection setup or the
 * connection/communication to the target data storage itself.
 */
public class IndexerRelatedIndexingException extends IndexingException {

  /** Required for implementations of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 486509140109072537L;

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   */
  public IndexerRelatedIndexingException(String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message The message. Can be null.
   * @param cause The cause. Can be null.
   */
  public IndexerRelatedIndexingException(String message, Exception cause) {
    super(message, cause);
  }
}
