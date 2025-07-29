package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import eu.europeana.indexing.common.contract.IndexerForSearching;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.MongoIndexer;
import eu.europeana.indexing.record.v2.MongoIndexingSettings;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class SimpleIndexerFactoryTest {

  private final IndexingJobFactory indexingJobFactory = new IndexingJobFactory();

  @Test
  void getSolrIndexer() throws SetupRelatedIndexingException, URISyntaxException {
    SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(SetupRelatedIndexingException::new);
    solrProperties.addSolrHost(new URI("http://localhost:8983"));
    assertInstanceOf(IndexerForSearching.class, indexingJobFactory.createIndexerForSearching(solrProperties));
  }

  @Test
  void getMongoIndexer() throws SetupRelatedIndexingException {
    MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
    mongoProperties.setMongoHosts(new String[]{"localhost"},new int[]{27001});
    MongoIndexingSettings settings = new MongoIndexingSettings(mongoProperties);
    settings.setMongoDatabaseName("recordDB");
    settings.setMongoTombstoneDatabaseName("tombstoneRecordDB");
    settings.setRecordRedirectDatabaseName("recordRedirectDB");

    assertInstanceOf(MongoIndexer.class, indexingJobFactory.getIndexer(settings));
    assertEquals("recordDB", settings.getMongoDatabaseName());
    assertEquals("tombstoneRecordDB", settings.getMongoTombstoneDatabaseName());
    assertEquals("recordRedirectDB", settings.getRecordRedirectDatabaseName());
  }
}
