package eu.europeana.indexing;

import eu.europeana.indexing.common.contract.IndexerForSearching;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.MongoIndexer;
import eu.europeana.indexing.record.v2.MongoIndexingSettings;
import eu.europeana.indexing.search.v2.IndexerForSearchingV2;
import eu.europeana.metis.common.SettingsHolder;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.util.Objects;

/**
 * A factory for specific indexing jobs (i.e., individual steps in the indexing process).
 */
public class IndexingJobFactory {

  /**
   * Gets an indexer for search.
   * @param solrProperties The solr properties.
   * @return the indexer.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public IndexerForSearching createIndexerForSearching(
      SolrProperties<SetupRelatedIndexingException> solrProperties)
      throws SetupRelatedIndexingException {
    Objects.requireNonNull(solrProperties, "solrProperties must not be null");
    return new IndexerForSearchingV2(new SolrClientProvider<>(solrProperties));
  }

  /**
   * Gets an indexer.
   * @param settings the settings can be either a SolrProperties or MongoProperties object.
   * @return the indexer {@link SimpleIndexer} pointing to mongo or solr.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public SimpleIndexer getIndexer(SettingsHolder settings) throws SetupRelatedIndexingException {
    if (settings instanceof MongoIndexingSettings mongoIndexingSettings) {
      return new MongoIndexer(mongoIndexingSettings);
    } else {
      throw new IllegalArgumentException("Invalid property configuration");
    }
  }
}
