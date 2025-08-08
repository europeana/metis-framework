package eu.europeana.indexing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.RedirectPersistence;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.RedirectionNotSupportedIndexingException;
import eu.europeana.indexing.common.exception.TombstoneHandlingNotSupportedIndexingException;
import eu.europeana.indexing.record.v2.RecordPersistenceV2;
import eu.europeana.indexing.record.v2.TombstonePersistenceV2;
import eu.europeana.indexing.redirect.v2.RedirectPersistenceV2;
import eu.europeana.indexing.search.v2.SearchPersistenceV2;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.util.Objects;
import org.apache.solr.client.solrj.SolrClient;

/**
 * This class is an implementation of {@link PersistenceAccessForIndexing} that sets up persistence
 * access using provided Solr and Mongo clients. Note: the caller is responsible for closing those
 * connections.
 */
record ClientsPersistenceAccess(RecordDao recordDao, RecordDao tombstoneRecordDao,
                                RecordRedirectDao recordRedirectDao, SolrClient solrClient)
    implements PersistenceAccessForIndexing<FullBeanImpl> {

  /**
   * Constructor.
   *
   * @param recordDao          The Mongo dao to be used. Cannot be null.
   * @param tombstoneRecordDao The Mongo tombstone dao to be used. If this is null, this persistence
   *                           access will not support tombstone handling.
   * @param recordRedirectDao  The record redirect dao. If this is null, this persistence
   *                           access will not support redirection.
   * @param solrClient         The Solr client to be used. Cannot be null.
   */
  public ClientsPersistenceAccess {
    Objects.requireNonNull(recordDao, "recordDao cannot be null");
    Objects.requireNonNull(solrClient, "solrClient cannot be null");
  }

  @Override
  public RecordPersistenceV2 getRecordPersistence() {
    return new RecordPersistenceV2(recordDao);
  }

  @Override
  public RedirectPersistence getRedirectPersistence()
      throws RedirectionNotSupportedIndexingException {
    if (recordRedirectDao == null) {
      throw new RedirectionNotSupportedIndexingException();
    }
    return new RedirectPersistenceV2(recordRedirectDao, getSearchPersistence());
  }

  @Override
  public SearchPersistenceV2 getSearchPersistence() {
    return new SearchPersistenceV2(solrClient);
  }

  @Override
  public TombstonePersistenceV2 getTombstonePersistence()
      throws TombstoneHandlingNotSupportedIndexingException {
    if (tombstoneRecordDao == null) {
      throw new TombstoneHandlingNotSupportedIndexingException();
    }
    return new TombstonePersistenceV2(tombstoneRecordDao, getRecordPersistence());
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    // This should be sufficient as we only have one Mongo connection and one Solr connection.
    getRecordPersistence().triggerFlushOfPendingChanges(blockUntilComplete);
    getSearchPersistence().triggerFlushOfPendingChanges(blockUntilComplete);
  }

  @Override
  public void close() {
    // Nothing to do: the clients are to be closed by the caller.
  }
}
