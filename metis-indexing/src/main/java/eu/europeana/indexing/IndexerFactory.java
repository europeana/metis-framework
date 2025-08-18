package eu.europeana.indexing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import org.apache.solr.client.solrj.SolrClient;

/**
 * This is a factory class for integrated indexers. It creates instances of {@link Indexer}.
 * @param <T> Tombstone type.
 */
public class IndexerFactory<T> {

  private final IndexerConnectionSupplier<T> connectionProviderSupplier;

  /**
   * Constructor for setting up a factory using an {@link IndexerConnectionSupplier}. For each
   * indexer that's created using this factory, the method {@link IndexerConnectionSupplier#get()}
   * will be called exactly once. Note: closing any indexer created using this object (i.e. calling
   * {@link Indexer#close()}) will result in a call to {@link PersistenceAccessForIndexing#close()} on
   * its connection provider.
   *
   * @param connectionProviderSupplier A supplier for connection providers.
   */
  public IndexerFactory(IndexerConnectionSupplier<T> connectionProviderSupplier) {
    this.connectionProviderSupplier = connectionProviderSupplier;
  }

  /**
   * Creator for setting up a factory using an {@link IndexingSettings} object.
   *
   * @param settings The settings to be applied to the indexer.
   * @return A factory for indexer instances.
   */
  public static IndexerFactory<FullBeanImpl> create(IndexingSettings settings) {
    return new IndexerFactory<>(() -> new SettingsPersistenceAccess(settings));
  }

  /**
   * Creator for setting up a factory using already existing Mongo and Solr clients. Note: the
   * caller is responsible for closing the clients. Any indexers created through the {@link
   * #getIndexer()} method will then no longer work and no new ones can be created. This method
   * facilitates creating indexer instances that don't support tombstone handling.
   *
   * @param recordDao The Mongo dao to use.
   * @param recordRedirectDao The record redirect dao. If null, an indexer will be created that
   *                          does not support redirection.
   * @param solrClient The Solr client to use.
   * @return A factory for indexer instances.
   */
  public static IndexerFactory<FullBeanImpl> create(RecordDao recordDao,
      RecordRedirectDao recordRedirectDao, SolrClient solrClient) {
    return create(recordDao, null, recordRedirectDao, solrClient);
  }

  /**
   * Creator for setting up a factory using already existing Mongo and Solr clients. Note: the
   * caller is responsible for closing the clients. Any indexers created through the {@link
   * #getIndexer()} method will then no longer work and no new ones can be created.
   *
   * @param recordDao The Mongo dao to use.
   * @param tombstoneRecordDao The Mongo tombstone dao to use. If null, indexer instances will be
   *                           created that don't support tombstone handling.
   * @param recordRedirectDao The record redirect dao. If null, indexer instances will be created
   *                          that don't support redirection.
   * @param solrClient The Solr client to use.
   * @return A factory for indexer instances.
   */
  public static IndexerFactory<FullBeanImpl> create(RecordDao recordDao, RecordDao tombstoneRecordDao,
      RecordRedirectDao recordRedirectDao, SolrClient solrClient) {
    return new IndexerFactory<>(() -> new ClientsPersistenceAccess(recordDao, tombstoneRecordDao,
        recordRedirectDao, solrClient));
  }

  /**
   * This method creates an indexer using the settings provided at construction.
   *
   * @return An indexer.
   * @throws SetupRelatedIndexingException In case an exception occurred while setting up the
   * indexer.
   */
  public Indexer<T> getIndexer() throws SetupRelatedIndexingException {
    try {
      return new IndexerImpl<>(connectionProviderSupplier.get());
    } catch (IllegalArgumentException e) {
      throw new SetupRelatedIndexingException("Creating a connection from the supplier failed.", e);
    }
  }

  /**
   * A supplier for instances of {@link PersistenceAccessForIndexing} that may throw an {@link
   * SetupRelatedIndexingException}.
   * @param <T> Tombstone type.
   */
  @FunctionalInterface
  public interface IndexerConnectionSupplier<T> {

    /**
     * Gets a result.
     *
     * @return A result.
     * @throws SetupRelatedIndexingException In case something went wrong while getting the result.
     * result.
     */
    PersistenceAccessForIndexing<T> get() throws SetupRelatedIndexingException;
  }
}
