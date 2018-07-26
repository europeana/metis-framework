package eu.europeana.indexing;

import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.exception.IndexerConfigurationException;

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

  private static final boolean DEFAULT_COMPUTE_UPDATE_AND_CREATE_TIMES = true;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerFactory.class);

  private final IndexerConnectionSupplier connectionProviderSupplier;

  /**
   * Constructor for setting up a factory using an {@link IndexingSettings} object.
   * 
   * @param settings The settings to be applied to the indexer.
   */
  public IndexerFactory(IndexingSettings settings) {
    this(() -> new SettingsConnectionProvider(settings));
  }

  /**
   * Constructor for setting up a factory using already existing Mongo and Solr clients. Note: the
   * caller is responsible for closing the clients. Any indexers created through the
   * {@link #getIndexer(boolean)} method will then no longer work and no new ones can be created.
   * 
   * @param mongoClient The Mongo client to use.
   * @param solrClient The Solr client to use.
   */
  public IndexerFactory(EdmMongoServer mongoClient, SolrClient solrClient) {
    this(() -> new ClientsConnectionProvider(mongoClient, solrClient));
  }

  /**
   * Constructor for setting up a factory using an {@link IndexerConnectionSupplier}. For each
   * indexer that's created using this factory, the method {@link IndexerConnectionSupplier#get()}
   * will be called exactly once. Note: closing any indexer created using this object (i.e. calling
   * {@link Indexer#close()}) will result in a call to {@link AbstractConnectionProvider#close()} on
   * its connection provider.
   * 
   * @param connectionProviderSupplier A supplier for connection providers.
   */
  public IndexerFactory(IndexerConnectionSupplier connectionProviderSupplier) {
    this.connectionProviderSupplier = connectionProviderSupplier;
  }

  /**
   * This method creates an indexer using the settings provided at construction. The behavior of
   * this indexer will be to compute new update and create times. See {@link #getIndexer(boolean)}.
   * 
   * @return An indexer.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  public Indexer getIndexer() throws IndexerConfigurationException {
    return getIndexer(DEFAULT_COMPUTE_UPDATE_AND_CREATE_TIMES);
  }

  /**
   * This method creates an indexer using the settings provided at construction.
   * 
   * @param computeUpdateAndCreateTimes This determines whether this indexer should use the updated
   *        and created times from the incoming RDFs, or whether it computes its own.
   * @return An indexer.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  public Indexer getIndexer(boolean computeUpdateAndCreateTimes)
      throws IndexerConfigurationException {
    try {
      return new IndexerImpl(connectionProviderSupplier.get(), computeUpdateAndCreateTimes);
    } catch (IndexerConfigurationException e) {
      LOGGER.warn("Error while setting up an indexer.", e);
      throw e;
    }
  }

  /**
   * A supplier for instances of {@link AbstractConnectionProvider} that may throw an
   * {@link IndexerConfigurationException}.
   * 
   * @author jochen
   */
  @FunctionalInterface
  public interface IndexerConnectionSupplier {

    /**
     * Gets a result.
     * 
     * @return A result.
     * @throws IndexerConfigurationException In case something went wrong while getting the result.
     */
    public AbstractConnectionProvider get() throws IndexerConfigurationException;
  }
}
