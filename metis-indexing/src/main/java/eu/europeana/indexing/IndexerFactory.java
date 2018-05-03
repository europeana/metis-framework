package eu.europeana.indexing;

import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.indexing.exception.IndexerConfigurationException;

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerFactory.class);

  private final IndexerConfigurationSupplier<AbstractConnectionProvider> connectionProviderSupplier;

  /**
   * Constructor for setting up a factory using an {@link IndexingSettings} object.
   * 
   * @param settings The settings to be applied to the indexer.
   */
  public IndexerFactory(IndexingSettings settings) {
    this.connectionProviderSupplier = () -> new SettingsConnectionProvider(settings);
  }

  /**
   * Constructor for setting up a factory using already existing Mongo and Solr clients. Note: the
   * caller is responsible for closing the clients. Any indexers created through the
   * {@link #getIndexer()} method will then no longer work and no new ones can be created.
   * 
   * @param mongoClient The Mongo client to use.
   * @param solrClient The Solr client to use.
   */
  public IndexerFactory(EdmMongoServer mongoClient, SolrClient solrClient) {
    this.connectionProviderSupplier = () -> new ClientsConnectionProvider(mongoClient, solrClient);
  }

  /**
   * This method creates an indexer using the settings provided at construction.
   * 
   * @return An indexer.
   * @throws IndexerConfigurationException In case an exception occurred while setting up the
   *         indexer.
   */
  public Indexer getIndexer() throws IndexerConfigurationException {
    try {
      return new IndexerImpl(connectionProviderSupplier.get());
    } catch (IndexerConfigurationException e) {
      LOGGER.warn("Error while setting up an indexer.", e);
      throw e;
    }
  }


  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an
   * {@link IndexerConfigurationException}.
   * 
   * @author jochen
   *
   * @param <T> The type of the object to be supplied.
   */
  @FunctionalInterface
  interface IndexerConfigurationSupplier<T> {

    /**
     * Gets a result.
     * 
     * @return A result.
     * @throws IndexerConfigurationException In case something went wrong while getting the result.
     */
    public T get() throws IndexerConfigurationException;
  }
}
