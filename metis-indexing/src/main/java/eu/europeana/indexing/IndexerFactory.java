package eu.europeana.indexing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.indexing.exception.IndexerConfigurationException;

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerFactory.class);

  private final IndexingSettings settings;

  /**
   * Constructor.
   * 
   * @param settings The settings to be applied to the indexer.
   */
  public IndexerFactory(IndexingSettings settings) {
    this.settings = settings;
  }

  /**
   * This method creates an indexer using the settings provided at construction.
   * 
   * @return An indexer.
   * @throws IndexerConfigurationException
   */
  public Indexer getIndexer() throws IndexerConfigurationException {
    try {
      return new IndexerImpl(settings);
    } catch (IndexerConfigurationException e) {
      LOGGER.warn("Error while setting up an indexer.", e);
      throw e;
    }
  }
}
