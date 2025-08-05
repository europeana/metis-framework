package eu.europeana.indexing;

import com.mongodb.client.MongoClient;
import eu.europeana.indexing.common.contract.IndexerForPersistence;
import eu.europeana.indexing.common.contract.IndexerForSearch;
import eu.europeana.indexing.common.contract.IndexerForTombstones;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.IndexerForPersistenceV2;
import eu.europeana.indexing.record.v2.IndexerForTombstonesV2;
import eu.europeana.indexing.search.v2.IndexerForSearchV2;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.dao.RecordDao;
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
  public IndexerForSearch createIndexerForSearch(
      SolrProperties<SetupRelatedIndexingException> solrProperties)
      throws SetupRelatedIndexingException {
    Objects.requireNonNull(solrProperties, "solrProperties must not be null");
    return new IndexerForSearchV2(new SolrClientProvider<>(solrProperties));
  }

  /**
   * Gets an indexer for persistence.
   * @param mongoProperties The mongo properties.
   * @param mongoDatabaseName The database name for the Mongo database.
   * @return the indexer.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public IndexerForPersistence createIndexerForPersistence(
      MongoProperties<SetupRelatedIndexingException> mongoProperties,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    final MongoClient client = new MongoClientProvider<>(mongoProperties).createMongoClient();
    return new IndexerForPersistenceV2(new RecordDao(client, mongoDatabaseName));
  }

  /**
   * Gets an indexer for tombstones. This indexer will not be suitable for creating a tombstone
   * for a live record.
   * @param mongoProperties The mongo properties.
   * @param mongoDatabaseName The database name for the Mongo database.
   * @return the indexer.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public IndexerForTombstones createIndexerForTombstones(
      MongoProperties<SetupRelatedIndexingException> mongoProperties,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    final MongoClient client = new MongoClientProvider<>(mongoProperties).createMongoClient();
    return new IndexerForTombstonesV2(null, new RecordDao(client, mongoDatabaseName));
  }
}
