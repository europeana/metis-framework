package eu.europeana.indexing;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.mongo.MongoIndexer;
import eu.europeana.indexing.mongo.MongoIndexingSettings;
import eu.europeana.indexing.solr.SolrIndexer;
import eu.europeana.indexing.solr.SolrIndexingSettings;
import eu.europeana.metis.common.SettingsHolder;

/**
 * The type Simple indexer factory.
 */
public class SimpleIndexerFactory {

  /**
   * Gets an indexer.
   * @param settings the settings can be either a SolrProperties or MongoProperties object.
   * @return the indexer {@link SimpleIndexer} pointing to mongo or solr.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public SimpleIndexer getIndexer(SettingsHolder settings) throws SetupRelatedIndexingException {
    if (settings instanceof SolrIndexingSettings) {
      return new SolrIndexer((SolrIndexingSettings) settings);
    } else if (settings instanceof MongoIndexingSettings) {
      return new MongoIndexer((MongoIndexingSettings) settings);
    } else {
      throw new IllegalArgumentException("Invalid property configuration");
    }
  }
}
