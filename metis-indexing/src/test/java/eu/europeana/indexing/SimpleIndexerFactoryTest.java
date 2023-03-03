package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.mongo.MongoIndexer;
import eu.europeana.indexing.solr.SolrIndexer;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrProperties;
import org.junit.jupiter.api.Test;

class SimpleIndexerFactoryTest {

  private final SimpleIndexerFactory simpleIndexerFactory = new SimpleIndexerFactory();

  @Test
  void getSolrIndexer() {
    SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(SetupRelatedIndexingException::new);
    assertTrue(simpleIndexerFactory.getIndexer(solrProperties) instanceof SolrIndexer);
  }

  @Test
  void getMongoIndexer() {
    MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
    assertTrue(simpleIndexerFactory.getIndexer(mongoProperties) instanceof MongoIndexer);
  }
}
