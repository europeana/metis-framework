package eu.europeana.indexing.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.indexing.base.TestContainer;
import eu.europeana.indexing.base.TestContainerFactoryIT;
import eu.europeana.indexing.base.TestContainerType;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.solr.SolrIndexerTest.SolrIndexerLocalConfigTest;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SolrIndexerLocalConfigTest.class)
class SolrIndexerTest {

  @Autowired
  private SolrIndexer indexer;

  @DynamicPropertySource
  public static void dynamicProperties(DynamicPropertyRegistry registry) {
    TestContainer solrContainerIT = TestContainerFactoryIT.getContainer(TestContainerType.SOLR);
    solrContainerIT.dynamicProperties(registry);
  }

  @Test
  void IllegalArgumentExceptionTest() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> indexer.indexRecord((RDF) null));
    assertEquals("Input RDF cannot be null.", expected.getMessage());
  }

  @Test
  void indexRecord() throws IndexingException {
//    final RDF inputRdf = new RDF();
//    indexer.indexRecord(inputRdf);
  }

  @Test
  void testIndexRecord() throws IndexingException {
//    final String stringRdfRecord = "";
//    indexer.indexRecord(stringRdfRecord);
  }

  @Test
  void indexRecordEmptyRecord_ExpectException() {
    final RDF inputRdf = new RDF();
    assertThrows(RecordRelatedIndexingException.class, ()-> indexer.indexRecord(inputRdf));
  }

  @Test
  void indexRecordEmptyStringRecord_ExpectException() {
    final String stringRdfRecord = "";
    assertThrows(RecordRelatedIndexingException.class, ()-> indexer.indexRecord(stringRdfRecord));
  }

  @Configuration
  static class SolrIndexerLocalConfigTest {

    @Bean
    SolrProperties<SetupRelatedIndexingException> solrProperties(@Value("${metis.test.publish.solr.hosts}") String solrHost)
        throws URISyntaxException, SetupRelatedIndexingException {
      SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(
          SetupRelatedIndexingException::new);
      solrProperties.addSolrHost(new URI(solrHost));
      return solrProperties;
    }

    @Bean
    SolrIndexer indexer(SolrProperties solrProperties) throws SetupRelatedIndexingException {
      return new SolrIndexer(solrProperties);
    }
  }
}
