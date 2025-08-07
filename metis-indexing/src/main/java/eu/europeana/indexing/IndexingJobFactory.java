package eu.europeana.indexing;

import eu.europeana.indexing.common.contract.RecordPersistence;
import eu.europeana.indexing.common.contract.SearchPersistence;
import eu.europeana.indexing.common.contract.TombstonePersistence;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.RecordPersistenceV2;
import eu.europeana.indexing.record.v2.TombstonePersistenceV2;
import eu.europeana.indexing.search.v2.SearchPersistenceV2;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
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
  public SearchPersistence<?, ?> createIndexerForSearch(
      SolrProperties<SetupRelatedIndexingException> solrProperties)
      throws SetupRelatedIndexingException {
    Objects.requireNonNull(solrProperties, "solrProperties must not be null");
    return new SearchPersistenceV2(new SolrClientProvider<>(solrProperties));
  }

  /**
   * Gets an indexer for persistence.
   * @param mongoProperties The mongo properties.
   * @param mongoDatabaseName The database name for the Mongo database.
   * @return the indexer.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public RecordPersistence<?> createIndexerForPersistence(
      MongoProperties<SetupRelatedIndexingException> mongoProperties,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    return new RecordPersistenceV2(new MongoClientProvider<>(mongoProperties), mongoDatabaseName);
  }

  /**
   * Gets an indexer for tombstones. This indexer will not be suitable for creating a tombstone
   * for a live record.
   * @param mongoProperties The mongo properties.
   * @param mongoDatabaseName The database name for the Mongo database.
   * @return the indexer.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public TombstonePersistence createIndexerForTombstones(
      MongoProperties<SetupRelatedIndexingException> mongoProperties,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    return new TombstonePersistenceV2(new MongoClientProvider<>(mongoProperties),
        mongoDatabaseName, null);
  }
}
