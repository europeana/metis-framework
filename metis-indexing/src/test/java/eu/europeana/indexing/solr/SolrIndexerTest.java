package eu.europeana.indexing.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.base.TestContainer;
import eu.europeana.indexing.base.TestContainerFactoryIT;
import eu.europeana.indexing.base.TestContainerType;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.solr.SolrIndexerTest.SolrIndexerLocalConfigTest;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
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

  @Autowired
  private SolrClient solrClient;

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
  void indexRecord() throws IndexingException, SerializationException, SolrServerException, IOException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf =  conversionUtils.convertStringToRdf(IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));

    indexer.indexRecord(inputRdf);

    flushAndAssertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7");
  }

  @Test
  void testIndexRecord() throws IndexingException, SolrServerException, IOException {
    final String stringRdfRecord = IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_string.xml");

    indexer.indexRecord(stringRdfRecord);

    flushAndAssertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_126_10");
  }

  private void flushAndAssertDocumentInSolr(String expected)
      throws SolrServerException, IOException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    solrClient.commit();
    SolrDocumentList documents = IndexingTestUtils.getSolrDocuments(solrClient, "europeana_id:\""+expected+ "\"");
    assertTrue(documents.size() == 1);
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
      SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(SetupRelatedIndexingException::new);
      solrProperties.addSolrHost(new URI(solrHost));
      return solrProperties;
    }
    @Bean
    SolrIndexingSettings solrIndexingSettings(SolrProperties properties) throws SetupRelatedIndexingException {
      return new SolrIndexingSettings(properties);
    }

    @Bean
    SolrClient solrClient(SolrProperties properties) throws Exception {
      return new SolrClientProvider<>(properties).createSolrClient().getSolrClient();
    }

    @Bean
    SolrIndexer indexer(SolrIndexingSettings settings) throws SetupRelatedIndexingException {
      return new SolrIndexer(settings);
    }
  }
}
