package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import java.io.Closeable;

/**
 * Implementations of this interface access and index data persistence database.
 */
public interface Persistence extends Closeable {

  /**
   * Flushes all pending changes to persistence.
   *
   * @param blockUntilComplete whether to perform this method synchronously (value
   *                           <code>true</code>) or asynchronously (value <code>false</code>).
   * @throws IndexerRelatedIndexingException in case there was a problem.
   */
  void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException;

}
