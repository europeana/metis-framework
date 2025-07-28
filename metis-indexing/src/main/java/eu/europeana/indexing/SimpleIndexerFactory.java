package eu.europeana.indexing;

import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.MongoIndexer;
import eu.europeana.indexing.record.v2.MongoIndexingSettings;
import eu.europeana.indexing.search.v2.SolrIndexer;
import eu.europeana.indexing.search.v2.SolrIndexingSettings;
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
    if (settings instanceof SolrIndexingSettings solrIndexingSettings) {
      return new SolrIndexer(solrIndexingSettings);
    } else if (settings instanceof MongoIndexingSettings mongoIndexingSettings) {
      return new MongoIndexer(mongoIndexingSettings);
    } else {
      throw new IllegalArgumentException("Invalid property configuration");
    }
  }
}
