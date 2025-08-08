package eu.europeana.indexing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.QueryableRecordPersistence;
import eu.europeana.indexing.common.contract.QueryableSearchPersistence;
import eu.europeana.indexing.common.contract.QueryableTombstonePersistence;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.RecordPersistenceV2;
import eu.europeana.indexing.record.v2.TombstonePersistenceV2;
import eu.europeana.indexing.search.v2.SearchPersistenceV2;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.util.Objects;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * A factory for specific indexing jobs (i.e., individual steps in the indexing process).
 */
public class IndexingJobFactory {

  /**
   * Gets an indexer for search.
   * @param solrProperties The solr properties.
   * @return the indexer, instance of {@link eu.europeana.indexing.common.contract.SearchPersistence}.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public QueryableSearchPersistence<SolrDocument, SolrDocumentList> createIndexerForSearch(
      SolrProperties<SetupRelatedIndexingException> solrProperties)
      throws SetupRelatedIndexingException {
    Objects.requireNonNull(solrProperties, "solrProperties must not be null");
    return new SearchPersistenceV2(new SolrClientProvider<>(solrProperties));
  }

  /**
   * Gets an indexer for persistence.
   * @param mongoProperties The mongo properties.
   * @param mongoDatabaseName The database name for the Mongo database.
   * @return the indexer, instance of {@link eu.europeana.indexing.common.contract.RecordPersistence}.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public QueryableRecordPersistence<FullBeanImpl> createIndexerForPersistence(
      MongoProperties<SetupRelatedIndexingException> mongoProperties,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    return new RecordPersistenceV2(new MongoClientProvider<>(mongoProperties), mongoDatabaseName);
  }

  /**
   * Gets an indexer for tombstones. This indexer will not be suitable for creating a tombstone
   * for a live record.
   * @param mongoProperties The mongo properties.
   * @param mongoDatabaseName The database name for the Mongo database.
   * @return the indexer, instance of {@link eu.europeana.indexing.common.contract.TombstonePersistence}.
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public QueryableTombstonePersistence<FullBeanImpl> createIndexerForTombstones(
      MongoProperties<SetupRelatedIndexingException> mongoProperties,
      String mongoDatabaseName) throws SetupRelatedIndexingException {
    return new TombstonePersistenceV2(new MongoClientProvider<>(mongoProperties),
        mongoDatabaseName, null);
  }
}
