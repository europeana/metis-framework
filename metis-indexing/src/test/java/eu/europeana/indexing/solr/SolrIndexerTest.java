package eu.europeana.indexing.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.IndexingProperties;
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
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

  private SolrDocument flushAndAssertDocumentInSolr(String expectedId, int expectedSize)
      throws SolrServerException, IOException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    solrClient.commit();
    final String solrQuery = String.format("%s:\"%s\"", EdmLabel.EUROPEANA_ID, ClientUtils.escapeQueryChars(expectedId));
    SolrDocumentList documents = IndexingTestUtils.getSolrDocuments(solrClient, solrQuery);
    LOGGER.info("documents");
    documents.stream().forEach(document -> {
      document.getFieldNames().stream().forEach(
          field -> LOGGER.info("{}:{}", field, document.getFieldValue(field))
      );
      assertThatDocumentFieldsExist(document);
    });
    assertEquals(expectedSize, documents.size());
    return documents.get(0);
  }

  private void assertThatDocumentFieldsExist(SolrDocument document) {
    List<String> expectedFields = List.of(
        "proxy_edm_type", "TYPE", "europeana_completeness", "COMPLETENESS", "europeana_collectionName", "edm_datasetName",
        "timestamp_created",
        "timestamp_update", "europeana_id", "dataProvider", "foaf_organization", "provider",
        "provider_aggregation_edm_dataProvider.en",
        "DATA_PROVIDER", "provider_aggregation_edm_dataProvider", "provider_aggregation_edm_provider.en", "PROVIDER",
        "provider_aggregation_edm_provider", "provider_aggregation_edm_rights.def", "RIGHTS", "provider_aggregation_edm_rights",
        "provider_aggregation_edm_isShownAt", "provider_aggregation_edm_isShownBy", "provider_aggregation_edm_object", "edm_UGC",
        "UGC",
        "edm_webResource", "wr_edm_rights.def", "wr_edm_rights", "europeana_aggregation_edm_country.def", "COUNTRY",
        "europeana_aggregation_edm_country", "europeana_aggregation_edm_language.def", "LANGUAGE",
        "europeana_aggregation_edm_language",
        "europeana_aggregation_edm_preview", "proxy_dc_creator.def", "CREATOR", "proxy_dc_creator", "who", "proxy_dc_date.def",
        "proxy_dc_date",
        "when", "proxy_dc_identifier.def", "proxy_dc_identifier", "proxy_dc_language.def", "proxy_dc_language",
        "proxy_dcterms_created.def",
        "proxy_dcterms_created", "proxy_dc_creator.nl", "proxy_dc_title.nl", "proxy_dc_title", "title", "proxy_dc_type.def",
        "proxy_dc_type",
        "proxy_dc_type_search", "what", "proxy_dcterms_isPartOf.def", "proxy_dcterms_isPartOf", "skos_concept",
        "cc_skos_prefLabel.def",
        "cc_skos_prefLabel", "cc_skos_altLabel.def", "cc_skos_altLabel", "cc_skos_prefLabel.nl", "cc_skos_prefLabel.en",
        "cc_skos_altLabel.en",
        "edm_timespan", "ts_skos_prefLabel.zxx", "ts_skos_prefLabel", "ts_skos_prefLabel.de", "ts_skos_prefLabel.ru",
        "ts_skos_prefLabel.fi",
        "ts_skos_prefLabel.pt", "ts_skos_prefLabel.bg", "ts_skos_prefLabel.lt", "ts_skos_prefLabel.lv", "ts_skos_prefLabel.hr",
        "ts_skos_prefLabel.fr", "ts_skos_prefLabel.hu", "ts_skos_prefLabel.sk", "ts_skos_prefLabel.sl", "ts_skos_prefLabel.ga",
        "ts_skos_prefLabel.ca", "ts_skos_prefLabel.sv", "ts_skos_prefLabel.el", "ts_skos_prefLabel.en", "ts_skos_prefLabel.it",
        "ts_skos_prefLabel.es", "ts_skos_prefLabel.et", "ts_skos_prefLabel.eu", "ts_skos_prefLabel.cs", "ts_skos_prefLabel.pl",
        "ts_skos_prefLabel.ro", "ts_skos_prefLabel.da", "ts_skos_prefLabel.nl", "ts_skos_altLabel.ru", "ts_skos_altLabel",
        "ts_skos_altLabel.sv", "ts_skos_altLabel.pt", "ts_skos_altLabel.en", "ts_skos_altLabel.it", "ts_skos_altLabel.fr",
        "edm_agent",
        "ag_skos_prefLabel.de", "ag_skos_prefLabel", "ag_skos_prefLabel.fi", "ag_skos_prefLabel.sv", "ag_skos_prefLabel.ru",
        "ag_skos_prefLabel.pt", "ag_skos_prefLabel.en", "ag_skos_prefLabel.it", "ag_skos_prefLabel.fr", "ag_skos_prefLabel.sl",
        "ag_skos_prefLabel.ga", "ag_skos_prefLabel.pl", "ag_skos_prefLabel.ro", "ag_skos_prefLabel.nl", "ag_skos_prefLabel.ca",
        "ag_skos_altLabel.de", "ag_skos_altLabel", "ag_skos_altLabel.ru", "ag_skos_altLabel.pt", "ag_skos_altLabel.en",
        "ag_skos_altLabel.it",
        "ag_skos_altLabel.pl", "ag_skos_altLabel.fr", "ag_skos_altLabel.ro", "ag_skos_altLabel.ca", "ag_skos_altLabel.nl",
        "ag_rdagr2_dateOfBirth.def", "ag_rdagr2_dateOfBirth", "ag_rdagr2_dateOfDeath.def", "ag_rdagr2_dateOfDeath",
        "ag_rdagr2_professionOrOccupation.def", "ag_rdagr2_professionOrOccupation", "has_thumbnails", "has_media",
        "has_landingpage",
        "is_fulltext", "filter_tags", "facet_tags", "_version_", "timestamp", "contentTier", "metadataTier"
    );
    expectedFields.forEach(expectedField ->
        assertTrue(document.getFieldNames().contains(expectedField), String.format("Expected %s is missing", expectedField))
    );

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
      SolrIndexingSettings solrIndexingSettings = new SolrIndexingSettings(properties);
      IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
          true,
          List.of(), true, true);
      solrIndexingSettings.setIndexingProperties(indexingProperties);
      return solrIndexingSettings;
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
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));

    indexer.indexRecord(inputRdf);

    final SolrDocument document = flushAndAssertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7", 1);
    assertEquals(Set.of("TEXT"), document.getFieldValues("proxy_edm_type").stream().collect(Collectors.toSet()));
    assertEquals(Set.of("TEXT"), document.getFieldValues("TYPE").stream().collect(Collectors.toSet()));
    assertEquals(Set.of("https://archivesspace-pi.nl/repositories/11/archival_objects/42779",
            "https://archivesspace-ed.nl/digital_objects/258/280/062-007.pdf"),
        document.getFieldValues("edm_webResource").stream().collect(Collectors.toSet()));
    assertEquals("8", document.getFieldValue("europeana_completeness").toString());
    assertEquals("8", document.getFieldValue("COMPLETENESS").toString());
    assertEquals("4", document.getFieldValue("contentTier").toString());
    assertEquals("B", document.getFieldValue("metadataTier").toString());
    assertEquals("/50/_providedCHO_NL_BwdADRKF_2_62_7", document.getFieldValue("europeana_id").toString());
    assertEquals("true", document.getFieldValue("has_thumbnails").toString());
    assertEquals("true", document.getFieldValue("has_media").toString());
    assertEquals("true", document.getFieldValue("has_landingpage").toString());
    assertEquals("false", document.getFieldValue("is_fulltext").toString());
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
  void indexExistingDateRecord() throws IndexingException, SerializationException, SolrServerException, IOException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));
    // add record
    indexer.indexRecord(inputRdf);

    // commit changes
    solrClient.commit();

    final RDF inputRdf2 = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));

    // add again same record to have created and update date
    indexer.indexRecord(inputRdf2);

    final SolrDocument document = flushAndAssertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7", 1);
    assertEquals(Set.of("TEXT"), document.getFieldValues("proxy_edm_type").stream().collect(Collectors.toSet()));
    assertEquals(Set.of("TEXT"), document.getFieldValues("TYPE").stream().collect(Collectors.toSet()));
    assertEquals(Set.of("https://archivesspace-pi.nl/repositories/11/archival_objects/42779",
            "https://archivesspace-ed.nl/digital_objects/258/280/062-007.pdf"),
        document.getFieldValues("edm_webResource").stream().collect(Collectors.toSet()));
    assertEquals("8", document.getFieldValue("europeana_completeness").toString());
    assertEquals("8", document.getFieldValue("COMPLETENESS").toString());
    assertEquals("4", document.getFieldValue("contentTier").toString());
    assertEquals("B", document.getFieldValue("metadataTier").toString());
    assertEquals("/50/_providedCHO_NL_BwdADRKF_2_62_7", document.getFieldValue("europeana_id").toString());
    assertEquals("true", document.getFieldValue("has_thumbnails").toString());
    assertEquals("true", document.getFieldValue("has_media").toString());
    assertEquals("true", document.getFieldValue("has_landingpage").toString());
    assertEquals("false", document.getFieldValue("is_fulltext").toString());
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

    final SolrDocument document = flushAndAssertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_126_10", 1);
    assertEquals(Set.of("TEXT"), document.getFieldValues("proxy_edm_type").stream().collect(Collectors.toSet()));
    assertEquals(Set.of("TEXT"), document.getFieldValues("TYPE").stream().collect(Collectors.toSet()));
    assertEquals(Set.of("https://archivesspace-ed.nl/digital_objects/270/292/126-010.pdf",
            "https://archivesspace-pi.nl/repositories/11/archival_objects/44084"),
        document.getFieldValues("edm_webResource").stream().collect(Collectors.toSet()));
    assertEquals("10", document.getFieldValue("europeana_completeness").toString());
    assertEquals("10", document.getFieldValue("COMPLETENESS").toString());
    assertEquals("4", document.getFieldValue("contentTier").toString());
    assertEquals("B", document.getFieldValue("metadataTier").toString());
    assertEquals("/50/_providedCHO_NL_BwdADRKF_2_126_10", document.getFieldValue("europeana_id").toString());
    assertEquals("true", document.getFieldValue("has_thumbnails").toString());
    assertEquals("true", document.getFieldValue("has_media").toString());
    assertEquals("true", document.getFieldValue("has_landingpage").toString());
    assertEquals("false", document.getFieldValue("is_fulltext").toString());
  }

  /**
   * Index record empty record expect exception.
   */
  @Test
  void indexRecordEmptyRecord_ExpectException() {
    final RDF inputRdf = new RDF();
    assertThrows(RecordRelatedIndexingException.class, () -> indexer.indexRecord(inputRdf));
  }

  /**
   * Index record empty string record expect exception.
   */
  @Test
  void indexRecordEmptyStringRecord_ExpectException() {
    final String stringRdfRecord = "";
    assertThrows(RecordRelatedIndexingException.class, () -> indexer.indexRecord(stringRdfRecord));
  }
}
