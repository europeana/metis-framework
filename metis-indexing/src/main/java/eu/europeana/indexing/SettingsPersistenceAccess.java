package eu.europeana.indexing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.Persistence;
import eu.europeana.indexing.common.contract.QueryableRecordPersistence;
import eu.europeana.indexing.common.contract.QueryableSearchPersistence;
import eu.europeana.indexing.common.contract.QueryableTombstonePersistence;
import eu.europeana.indexing.common.contract.RedirectPersistence;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.RedirectionNotSupportedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.exception.TombstoneHandlingNotSupportedIndexingException;
import eu.europeana.indexing.record.v2.RecordPersistenceV2;
import eu.europeana.indexing.record.v2.TombstonePersistenceV2;
import eu.europeana.indexing.redirect.v2.RedirectPersistenceV2;
import eu.europeana.indexing.search.v2.SearchPersistenceV2;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * This class is an implementation of {@link PersistenceAccessForIndexing} that sets up the connection
 * using an {@link IndexingSettings} object. Various methods are made public so that this class may
 * be constructed and used outside the scope of the indexing library.
 */
public final class SettingsPersistenceAccess implements
    PersistenceAccessForIndexing<FullBeanImpl> {

  private final QueryableRecordPersistence<FullBeanImpl> recordPersistence;
  private final RedirectPersistence redirectPersistence;
  private final QueryableSearchPersistence<SolrDocument, SolrDocumentList> searchPersistence;
  private final QueryableTombstonePersistence<FullBeanImpl> tombstonePersistence;

  /**
   * Constructor. Sets up the required connections using the supplied settings.
   *
   * @param settings The indexing settings (connection settings).
   * @throws SetupRelatedIndexingException In case the connections could not be set up.
   */
  public SettingsPersistenceAccess(IndexingSettings settings)
      throws SetupRelatedIndexingException {

    // Sanity check
    Objects.requireNonNull(settings, "The provided settings object is null.");

    // Create the client providers
    final MongoClientProvider<SetupRelatedIndexingException> mongoClientProvider =
        new MongoClientProvider<>(settings.getMongoProperties());
    final SolrClientProvider<SetupRelatedIndexingException> solrClientProvider =
        new SolrClientProvider<>(settings.getSolrProperties());

    // Set up persistence access.
    this.recordPersistence = new RecordPersistenceV2(mongoClientProvider,
        settings.getMongoDatabaseName());
    this.searchPersistence = new SearchPersistenceV2(solrClientProvider);
    if (settings.getRecordRedirectDatabaseName() != null) {
      this.redirectPersistence = new RedirectPersistenceV2(mongoClientProvider,
          settings.getRecordRedirectDatabaseName(), this.searchPersistence);
    } else {
      this.redirectPersistence = null;
    }
    if (settings.getMongoTombstoneDatabaseName() != null) {
      this.tombstonePersistence = new TombstonePersistenceV2(mongoClientProvider,
          settings.getMongoTombstoneDatabaseName(), this.recordPersistence);
    } else {
      this.tombstonePersistence = null;
    }
  }

  @Override
  public QueryableRecordPersistence<FullBeanImpl> getRecordPersistence() {
    return this.recordPersistence;
  }

  @Override
  public RedirectPersistence getRedirectPersistence()
      throws RedirectionNotSupportedIndexingException {
    if (this.redirectPersistence == null) {
      throw new RedirectionNotSupportedIndexingException();
    }
    return this.redirectPersistence;
  }

  @Override
  public QueryableSearchPersistence<SolrDocument, SolrDocumentList> getSearchPersistence() {
    return this.searchPersistence;
  }

  @Override
  public QueryableTombstonePersistence<FullBeanImpl> getTombstonePersistence()
      throws TombstoneHandlingNotSupportedIndexingException {
    if (this.tombstonePersistence == null) {
      throw new TombstoneHandlingNotSupportedIndexingException();
    }
    return this.tombstonePersistence;
  }

  private List<Persistence> getPersistenceObjects() {
    return Stream.of(this.recordPersistence, this.redirectPersistence, this.searchPersistence,
        this.tombstonePersistence).filter(Objects::nonNull).toList();
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    for (Persistence persistence : getPersistenceObjects()) {
      persistence.triggerFlushOfPendingChanges(blockUntilComplete);
    }
  }

  @Override
  public void close() throws IOException {
    for (Persistence persistence : getPersistenceObjects()) {
      persistence.close();
    }
  }
}
