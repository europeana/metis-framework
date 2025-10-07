package eu.europeana.indexing.exception;

import java.io.Serial;

/**
 * Exception indicating that a tombstone related functionality was invoked on an indexer that
 * does not support tombstone handling.
 */
public class TombstoneHandlingNotSupportedIndexingException extends IndexingException {

  @Serial
  private static final long serialVersionUID = 3582800817492105493L;

  /**
   * Constructor.
   */
  public TombstoneHandlingNotSupportedIndexingException() {
    super("Tombstone handling not supported.");
  }
}
