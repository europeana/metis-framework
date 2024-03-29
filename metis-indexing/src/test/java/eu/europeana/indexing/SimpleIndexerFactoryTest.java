package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.mongo.MongoIndexer;
import eu.europeana.indexing.mongo.MongoIndexingSettings;
import eu.europeana.indexing.solr.SolrIndexer;
import eu.europeana.indexing.solr.SolrIndexingSettings;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class SimpleIndexerFactoryTest {

  private final SimpleIndexerFactory simpleIndexerFactory = new SimpleIndexerFactory();

  @Test
  void getSolrIndexer() throws SetupRelatedIndexingException, URISyntaxException {
    SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(SetupRelatedIndexingException::new);
    solrProperties.addSolrHost(new URI("http://localhost:8983"));
    SolrIndexingSettings settings = new SolrIndexingSettings(solrProperties);

    assertTrue(simpleIndexerFactory.getIndexer(settings) instanceof SolrIndexer);
  }

  @Test
  void getMongoIndexer() throws SetupRelatedIndexingException {
    MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
    mongoProperties.setMongoHosts(new String[]{"localhost"},new int[]{27001});
    MongoIndexingSettings settings = new MongoIndexingSettings(mongoProperties);
    settings.setMongoDatabaseName("recordDB");
    settings.setRecordRedirectDatabaseName("recordRedirectDB");

    assertTrue(simpleIndexerFactory.getIndexer(settings) instanceof MongoIndexer);
    assertEquals("recordDB", settings.getMongoDatabaseName());
    assertEquals("recordRedirectDB", settings.getRecordRedirectDatabaseName());
  }
}
