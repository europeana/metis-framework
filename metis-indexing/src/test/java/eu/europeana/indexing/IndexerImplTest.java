package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.indexing.IndexerImplTest.IndexerImplLocalConfigTest;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.base.TestContainer;
import eu.europeana.indexing.base.TestContainerFactoryIT;
import eu.europeana.indexing.base.TestContainerType;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import eu.europeana.indexing.tiers.TierCalculationMode;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import eu.europeana.metis.solr.connection.SolrProperties;
import eu.europeana.metis.utils.DepublicationReason;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The type Indexer impl test.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IndexerImplLocalConfigTest.class)
class IndexerImplTest {

  /**
   * The Indexer.
   */
  @Autowired
  IndexerImpl indexer;

  /**
   * The Settings connection provider.
   */
  @Autowired
  SettingsConnectionProvider settingsConnectionProvider;

  /**
   * The Solr client.
   */
  @Autowired
  SolrClient solrClient;

  /**
   * The Rdf conversion utils.
   */
  RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

  /**
   * The Indexing properties.
   */
  IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
      true,
      List.of(), true, TierCalculationMode.INITIALISE);

  /**
   * Dynamic properties.
   *
   * @param registry the registry
   */
  @DynamicPropertySource
  public static void dynamicProperties(DynamicPropertyRegistry registry) {
    TestContainer solrContainerIT = TestContainerFactoryIT.getContainer(TestContainerType.SOLR);
    solrContainerIT.dynamicProperties(registry);
    TestContainer mongoContainerIT = TestContainerFactoryIT.getContainer(TestContainerType.MONGO);
    mongoContainerIT.dynamicProperties(registry);
  }

  @BeforeEach
  void beforeEach() {
    reset(settingsConnectionProvider);
  }

  /**
   * Read file to string string.
   *
   * @param file the file
   * @return the string
   * @throws IOException the io exception
   */
  public static String readFileToString(String file) throws IOException {
    ClassLoader classLoader = IndexerImplTest.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(file);
    if (inputStream == null) {
      throw new IOException("Failed reading file " + file);
    }
    return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
  }

  private void assertDocumentInMongo(String expectedId) throws EuropeanaException {
    FullBean fullBean = this.settingsConnectionProvider.getRecordDao().getFullBean(expectedId);
    assertNotNull(fullBean);
    assertEquals(expectedId, fullBean.getAbout());
  }

  private void assertNotExistsDocumentInMongo(String expectedId) throws EuropeanaException {
    FullBean fullBean = this.settingsConnectionProvider.getRecordDao().getFullBean(expectedId);
    assertNull(fullBean);
  }

  private SolrDocument assertDocumentInSolr(String expectedId)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    final String solrQuery = String.format("%s:\"%s\"", SolrV2Field.EUROPEANA_ID, ClientUtils.escapeQueryChars(expectedId));
    SolrDocumentList documents = IndexingTestUtils.getSolrDocuments(solrClient, solrQuery);
    assertEquals(expectedId, documents.getFirst().get("europeana_id"));
    return documents.stream().findFirst().orElse(null);
  }

  private void assertNotExistsDocumentInSolr(String expectedId)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    final String solrQuery = String.format("%s:\"%s\"", SolrV2Field.EUROPEANA_ID, ClientUtils.escapeQueryChars(expectedId));
    SolrDocumentList documents = IndexingTestUtils.getSolrDocuments(solrClient, solrQuery);
    assertEquals(0, documents.size());
  }

  /**
   * The type Indexer impl local config test.
   */
  @Configuration
  static class IndexerImplLocalConfigTest {

    /**
     * Mongo properties mongo properties.
     *
     * @param mongoHost the mongo host
     * @param mongoPort the mongo port
     * @return the mongo properties
     * @throws SetupRelatedIndexingException the setup related indexing exception
     */
    @Bean
    MongoProperties<SetupRelatedIndexingException> mongoProperties(@Value("${mongo.hosts}") String mongoHost,
        @Value("${mongo.port}") int mongoPort) throws SetupRelatedIndexingException {
      MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
      mongoProperties.setMongoHosts(new String[]{mongoHost}, new int[]{mongoPort});
      return mongoProperties;
    }

    /**
     * Mongo client mongo client.
     *
     * @param mongoProperties the mongo properties
     * @return the mongo client
     * @throws Exception the exception
     */
    @Bean
    MongoClient mongoClient(MongoProperties<SetupRelatedIndexingException> mongoProperties) throws Exception {
      return new MongoClientProvider<>(mongoProperties).createMongoClient();
    }

    /**
     * Indexing settings indexing settings.
     *
     * @param mongoHost the mongo host
     * @param mongoPort the mongo port
     * @param mongoDatabase the mongo database
     * @param mongoTombstoneDatabase the mongo tombstone database
     * @param mongoRedirectDatabase the mongo redirect database
     * @param solrHost the solr host
     * @return the indexing settings
     * @throws SetupRelatedIndexingException the setup related indexing exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Bean
    IndexingSettings indexingSettings(@Value("${mongo.hosts}") String mongoHost,
        @Value("${mongo.port}") int mongoPort,
        @Value("${mongo.db}") String mongoDatabase,
        @Value("${mongo.tombstone.db}") String mongoTombstoneDatabase,
        @Value("${mongo.redirect.db}") String mongoRedirectDatabase,
        @Value("${metis.test.publish.solr.hosts}") String solrHost) throws SetupRelatedIndexingException, URISyntaxException {
      IndexingSettings indexingSettings = new IndexingSettings();
      indexingSettings.addMongoHost(new InetSocketAddress(mongoHost, mongoPort));
      indexingSettings.setMongoDatabaseName(mongoDatabase);
      indexingSettings.setMongoTombstoneDatabaseName(mongoTombstoneDatabase);
      indexingSettings.setRecordRedirectDatabaseName(mongoRedirectDatabase);
      indexingSettings.addSolrHost(new URI(solrHost));
      return indexingSettings;
    }

    /**
     * Sets connection provider.
     *
     * @param settings the settings
     * @return the connection provider
     * @throws SetupRelatedIndexingException the setup related indexing exception
     * @throws IndexerRelatedIndexingException the indexer related indexing exception
     */
    @Bean
    SettingsConnectionProvider settingsConnectionProvider(IndexingSettings settings)
        throws SetupRelatedIndexingException, IndexerRelatedIndexingException {
      return Mockito.spy(new SettingsConnectionProvider(settings));
    }

    /**
     * Indexer indexer.
     *
     * @param connectionProvider the connection provider
     * @return the indexer
     */
    @Bean
    IndexerImpl indexer(SettingsConnectionProvider connectionProvider) {
      return new IndexerImpl(connectionProvider);
    }

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
     * Solr client solr client.
     *
     * @param properties the properties
     * @return the solr client
     * @throws Exception the exception
     */
    @Bean
    SolrClient solrClient(SolrProperties<SetupRelatedIndexingException> properties) throws Exception {
      return new SolrClientProvider<>(properties).createSolrClient().getSolrClient();
    }
  }

  /**
   * Index rdfs.
   *
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   * @throws EuropeanaException the europeana exception
   * @throws SolrServerException the solr server exception
   */
  @Test
  void indexRdfs() throws IOException, SerializationException, IndexingException, EuropeanaException, SolrServerException {

    final RDF rdf1 = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_to_sample_index_rdf.xml"));
    final RDF rdf2 = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));

    indexer.indexRdfs(List.of(rdf1, rdf2), indexingProperties);

    solrClient.commit();

    assertDocumentInMongo("/50/_providedCHO_NL_BwdADRKF_2_62_7");
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7");
    assertDocumentInSolr("/277/CMC_HA_1185");
  }

  /**
   * Index.
   *
   * @throws IOException the io exception
   * @throws IndexingException the indexing exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void index() throws IOException, IndexingException, SolrServerException, EuropeanaException {

    indexer.index(readFileToString("europeana_record_to_sample_index_rdf.xml"), indexingProperties);

    solrClient.commit();

    assertDocumentInMongo("/50/_providedCHO_NL_BwdADRKF_2_62_7");
    assertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7");
  }

  /**
   * Index and get tier calculations.
   *
   * @throws IOException the io exception
   * @throws IndexingException the indexing exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void indexAndGetTierCalculations() throws IOException, IndexingException, SolrServerException, EuropeanaException {

    TierResults tierResults = indexer.indexAndGetTierCalculations(
        new ByteArrayInputStream(readFileToString("europeana_record_to_sample_index_rdf.xml").getBytes(
            StandardCharsets.UTF_8)), indexingProperties);

    assertEquals(MediaTier.T4, tierResults.getMediaTier());
    assertEquals(MediaTier.T4, tierResults.getContentTierBeforeLicenseCorrection());
    assertEquals(MetadataTier.TB, tierResults.getMetadataTier());
    assertEquals(LicenseType.OPEN, tierResults.getLicenseType());
    assertEquals(MetadataTier.TB, tierResults.getMetadataTierLanguage());
    assertEquals(MetadataTier.TB, tierResults.getMetadataTierEnablingElements());
    assertEquals(MetadataTier.TB, tierResults.getMetadataTierContextualClasses());

    solrClient.commit();

    assertDocumentInMongo("/50/_providedCHO_NL_BwdADRKF_2_62_7");
    assertDocumentInSolr("/50/_providedCHO_NL_BwdADRKF_2_62_7");
  }

  /**
   * Index rdf.
   *
   * @throws IndexingException the indexing exception
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void indexRdf() throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));

    indexer.indexRdf(rdf, indexingProperties);

    solrClient.commit();

    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");
  }

  @Test
  void remove() throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));

    indexer.indexRdf(rdf, indexingProperties);
    solrClient.commit();
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");

    IndexedRecordAccess realAccess = settingsConnectionProvider.getIndexedRecordAccess();
    IndexedRecordAccess spyAccess = Mockito.spy(realAccess);
    doThrow(new IndexerRelatedIndexingException("", new SocketTimeoutException()))
        .doCallRealMethod().when(spyAccess).removeRecord("/277/CMC_HA_1185");
    when(settingsConnectionProvider.getIndexedRecordAccess()).thenReturn(spyAccess);

    indexer.remove("/277/CMC_HA_1185");
    solrClient.commit();
    assertNotExistsDocumentInMongo("/277/CMC_HA_1185");
    assertNotExistsDocumentInSolr("/277/CMC_HA_1185");
    verify(spyAccess, times(2)).removeRecord("/277/CMC_HA_1185");
  }

  /**
   * Gets tombstone.
   *
   * @throws IndexingException the indexing exception
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void getTombstone() throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));
    indexer.indexRdf(rdf, indexingProperties);
    solrClient.commit();
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");

    boolean result = indexer.indexTombstone("/277/CMC_HA_1185", DepublicationReason.GENERIC);
    assertTrue(result);

    IndexedRecordAccess realAccess = settingsConnectionProvider.getIndexedRecordAccess();
    IndexedRecordAccess spyAccess = Mockito.spy(realAccess);
    doThrow(new RuntimeException("", new SocketTimeoutException()))
        .doCallRealMethod().when(spyAccess).getTombstoneFullbean("/277/CMC_HA_1185");
    when(settingsConnectionProvider.getIndexedRecordAccess()).thenReturn(spyAccess);
    FullBean fullBean = indexer.getTombstone("/277/CMC_HA_1185");

    assertNotNull(fullBean);
    assertEquals("/277/CMC_HA_1185", fullBean.getAbout());
    verify(spyAccess, times(2)).getTombstoneFullbean("/277/CMC_HA_1185");
  }

  /**
   * Index and Remove tombstone.
   *
   * @throws IndexingException the indexing exception
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void indexAndRemoveTombstone()
      throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));
    indexer.indexRdf(rdf, indexingProperties);
    solrClient.commit();
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");

    // tombstoned
    boolean result = indexer.indexTombstone("/277/CMC_HA_1185", DepublicationReason.GENERIC);
    assertTrue(result);
    FullBean fullBean = indexer.getTombstone("/277/CMC_HA_1185");
    assertNotNull(fullBean);

    IndexedRecordAccess realAccess = settingsConnectionProvider.getIndexedRecordAccess();
    IndexedRecordAccess spyAccess = Mockito.spy(realAccess);
    doThrow(new IndexerRelatedIndexingException("", new SocketTimeoutException()))
        .doCallRealMethod().when(spyAccess).removeTombstone("/277/CMC_HA_1185");
    when(settingsConnectionProvider.getIndexedRecordAccess()).thenReturn(spyAccess);
    // removed from the tombstone
    result = indexer.removeTombstone("/277/CMC_HA_1185");
    verify(spyAccess, times(2)).removeTombstone("/277/CMC_HA_1185");
    assertTrue(result);
    // not in tombstone
    fullBean = indexer.getTombstone("/277/CMC_HA_1185");
    assertNull(fullBean);
  }


  /**
   * Temporarily not allowed to tombstone.
   *
   * @param depublicationReason the depublication reason
   * @throws IndexingException the indexing exception
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @ParameterizedTest
  @EnumSource(value = DepublicationReason.class,
      names = {"GDPR", "PERMISSION_ISSUES", "SENSITIVE_CONTENT"},
      mode = Mode.INCLUDE)
  void temporarilyNotAllowedToTombstone(DepublicationReason depublicationReason)
      throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));
    indexer.indexRdf(rdf, indexingProperties);
    solrClient.commit();
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");

    // not tombstoned just warning
    boolean result = indexer.indexTombstone("/277/CMC_HA_1185", depublicationReason);
    assertTrue(result);
    FullBean fullBean = indexer.getTombstone("/277/CMC_HA_1185");
    assertNull(fullBean);

    // not in tombstone
    fullBean = indexer.getTombstone("/277/CMC_HA_1185");
    assertNull(fullBean);

    // the record still in its place
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");
  }

  /**
   * Gets record ids.
   *
   * @throws IndexingException the indexing exception
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void getRecordIds() throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));
    indexer.indexRdf(rdf, indexingProperties);

    solrClient.commit();
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");

    Date now = Date.from(Instant.now());
    IndexedRecordAccess realAccess = settingsConnectionProvider.getIndexedRecordAccess();
    IndexedRecordAccess spyAccess = Mockito.spy(realAccess);
    doThrow(new RuntimeException("", new SocketTimeoutException()))
        .doCallRealMethod().when(spyAccess).getRecordIds("277", now);
    when(settingsConnectionProvider.getIndexedRecordAccess()).thenReturn(spyAccess);
    List<String> list = indexer.getRecordIds("277", now).toList();
    verify(spyAccess, times(2)).getRecordIds("277", now);

    assertEquals(1, list.size());
    assertEquals("/277/CMC_HA_1185", list.getFirst());
  }

  /**
   * Count records.
   *
   * @throws IndexingException the indexing exception
   * @throws IOException the io exception
   * @throws SerializationException the serialization exception
   * @throws SolrServerException the solr server exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void countRecords() throws IndexingException, IOException, SerializationException, SolrServerException, EuropeanaException {
    final RDF rdf = rdfConversionUtils.convertStringToRdf(readFileToString("europeana_record_rdf_conversion.xml"));
    indexer.indexRdf(rdf, indexingProperties);
    solrClient.commit();
    assertDocumentInMongo("/277/CMC_HA_1185");
    assertDocumentInSolr("/277/CMC_HA_1185");

    IndexedRecordAccess realAccess = settingsConnectionProvider.getIndexedRecordAccess();
    IndexedRecordAccess spyAccess = Mockito.spy(realAccess);
    doThrow(new RuntimeException("", new SocketTimeoutException()))
        .doCallRealMethod().when(spyAccess).countRecords("277");
    when(settingsConnectionProvider.getIndexedRecordAccess()).thenReturn(spyAccess);
    long result = indexer.countRecords("277");
    verify(spyAccess, times(2)).countRecords("277");
    assertEquals(1, result);
  }

}
