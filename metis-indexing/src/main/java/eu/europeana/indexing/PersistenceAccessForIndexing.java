package eu.europeana.indexing;

import eu.europeana.indexing.common.contract.QueryableTombstonePersistence;
import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.contract.RedirectPersistence;
import eu.europeana.indexing.common.contract.SearchPersistence;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.RedirectionNotSupportedIndexingException;
import eu.europeana.indexing.common.exception.TombstoneHandlingNotSupportedIndexingException;
import java.io.Closeable;

/**
 * Instances of this class are responsible for providing persistence access. It is responsible for
 * maintaining the connections. Please note that this class is {@link Closeable} and must be closed
 * to release its resources. Note: access providers may not support tombstone handling.
 * @param <T> The type of the tombstone that is returned.
 */
public interface PersistenceAccessForIndexing<T> extends Closeable {

  /**
   * This method will trigger a flush operation on pending changes/updates to the persistent data,
   * causing it to become permanent as well as available to other processes. Calling this method is
   * not obligatory, and indexing will work without it. This just allows the caller to determine the
   * moment when changes are written to disk rather than wait for this to be triggered by the
   * infrastructure/library itself at its own discretion.
   *
   * @param blockUntilComplete If true, the call blocks until the flush is complete.
   * @throws IndexerRelatedIndexingException If there is some issue.
   */
  void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException;

  /**
   * Provide record persistence access.
   *
   * @return Record persistence access.
   */
  RecordPersistence getRecordPersistence();

  /**
   * Provide redirect persistence access.
   *
   * @return Redirect persistence access.
   * @throws RedirectionNotSupportedIndexingException if this access provider does not support
   *                                                  redirection.
   */
  RedirectPersistence getRedirectPersistence() throws RedirectionNotSupportedIndexingException;

  /**
   * Provide search persistence access.
   *
   * @return Search persistence access.
   */
  SearchPersistence getSearchPersistence();

  /**
   * Provide tombstone persistence access.
   *
   * @return Tombstone persistence access.
   * @throws TombstoneHandlingNotSupportedIndexingException if this access provider does not support
   *                                                        tombstone handling.
   */
  QueryableTombstonePersistence<T> getTombstonePersistence()
      throws TombstoneHandlingNotSupportedIndexingException;
}
