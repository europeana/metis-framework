package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexingException;
import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.mongo.server.EdmMongoServer;

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

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
   * {@link #getIndexer()} method will then no longer work and no new ones can be created.
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
   * This method creates an indexer using the settings provided at construction.
   * 
   * @return An indexer.
   * @throws IndexingException In case an exception occurred while setting up the
   *         indexer.
   */
  public Indexer getIndexer() throws IndexingException {
    try {
      return new IndexerImpl(connectionProviderSupplier.get());
    } catch (IndexingException e) {
      LOGGER.warn("Error while setting up an indexer.", e);
      throw e;
    }
  }

  /**
   * A supplier for instances of {@link AbstractConnectionProvider} that may throw an
   * {@link IndexingException}.
   * 
   * @author jochen
   */
  @FunctionalInterface
  public interface IndexerConnectionSupplier {

    /**
     * Gets a result.
     * 
     * @return A result.
     * @throws IndexingException In case something went wrong while getting the result.
     */
    AbstractConnectionProvider get() throws IndexingException;
  }
}
