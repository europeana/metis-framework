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

/**
 * The type Solr indexer test.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SolrIndexerLocalConfigTest.class)
class SolrIndexerTest {

  @Autowired
  private SolrIndexer indexer;

  @Autowired
  private SolrClient solrClient;

  /**
   * Dynamic properties.
   *
   * @param registry the registry
   */
  @DynamicPropertySource
  public static void dynamicProperties(DynamicPropertyRegistry registry) {
    TestContainer solrContainerIT = TestContainerFactoryIT.getContainer(TestContainerType.SOLR);
    solrContainerIT.dynamicProperties(registry);
  }

  /**
   * Illegal argument exception test.
   */
  @Test
  void IllegalArgumentExceptionTest() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> indexer.indexRecord((RDF) null));
    assertEquals("Input RDF cannot be null.", expected.getMessage());
  }

  /**
   * Index record.
   *
   * @throws IndexingException the indexing exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws IOException the io exception
   */
  @Test
  void indexRecord() throws IndexingException, SerializationException, SolrServerException, IOException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf =  conversionUtils.convertStringToRdf(IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));

    indexer.indexRecord(inputRdf);

    flushAndAssertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7");
  }

  /**
   * Test index record.
   *
   * @throws IndexingException the indexing exception
   * @throws SolrServerException the solr server exception
   * @throws IOException the io exception
   */
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

  /**
   * Index record empty record expect exception.
   */
  @Test
  void indexRecordEmptyRecord_ExpectException() {
    final RDF inputRdf = new RDF();
    assertThrows(RecordRelatedIndexingException.class, ()-> indexer.indexRecord(inputRdf));
  }

  /**
   * Index record empty string record expect exception.
   */
  @Test
  void indexRecordEmptyStringRecord_ExpectException() {
    final String stringRdfRecord = "";
    assertThrows(RecordRelatedIndexingException.class, ()-> indexer.indexRecord(stringRdfRecord));
  }

  /**
   * The type Solr indexer local config test.
   */
  @Configuration
  static class SolrIndexerLocalConfigTest {

    /**
     * Solr properties solr properties.
     *
     * @param solrHost the solr host
     * @return the solr properties
     * @throws URISyntaxException the uri syntax exception
     * @throws SetupRelatedIndexingException the setup related indexing exception
     */
    @Bean
    SolrProperties<SetupRelatedIndexingException> solrProperties(@Value("${metis.test.publish.solr.hosts}") String solrHost)
        throws URISyntaxException, SetupRelatedIndexingException {
      SolrProperties<SetupRelatedIndexingException> solrProperties = new SolrProperties<>(SetupRelatedIndexingException::new);
      solrProperties.addSolrHost(new URI(solrHost));
      return solrProperties;
    }

    /**
     * Solr indexing settings solr indexing settings.
     *
     * @param properties the properties
     * @return the solr indexing settings
     * @throws SetupRelatedIndexingException the setup related indexing exception
     */
    @Bean
    SolrIndexingSettings solrIndexingSettings(SolrProperties properties) throws SetupRelatedIndexingException {
      return new SolrIndexingSettings(properties);
    }

    /**
     * Solr client solr client.
     *
     * @param properties the properties
     * @return the solr client
     * @throws Exception the exception
     */
    @Bean
    SolrClient solrClient(SolrProperties properties) throws Exception {
      return new SolrClientProvider<>(properties).createSolrClient().getSolrClient();
    }

    /**
     * Indexer solr indexer.
     *
     * @param settings the settings
     * @return the solr indexer
     * @throws SetupRelatedIndexingException the setup related indexing exception
     */
    @Bean
    SolrIndexer indexer(SolrIndexingSettings settings) throws SetupRelatedIndexingException {
      return new SolrIndexer(settings);
    }
  }
}
