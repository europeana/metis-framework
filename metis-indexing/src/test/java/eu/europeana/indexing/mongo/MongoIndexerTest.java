package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.indexing.IndexingProperties;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.base.TestContainer;
import eu.europeana.indexing.base.TestContainerFactoryIT;
import eu.europeana.indexing.base.TestContainerType;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.mongo.MongoIndexerTest.MongoIndexerLocalConfigTest;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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
 * The type Mongo indexer test.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MongoIndexerLocalConfigTest.class)
class MongoIndexerTest {

  @Autowired
  private MongoIndexer indexer;

  @Autowired
  private RecordDao recordDao;

  /**
   * Dynamic properties.
   *
   * @param registry the registry
   */
  @DynamicPropertySource
  public static void dynamicProperties(DynamicPropertyRegistry registry) {
    TestContainer mongoContainerIT = TestContainerFactoryIT.getContainer(TestContainerType.MONGO);
    mongoContainerIT.dynamicProperties(registry);
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
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void indexRecord() throws IndexingException, SerializationException, EuropeanaException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));
    indexer.indexRecord(inputRdf);

    assertIndexedRecord("/50/_providedCHO_NL_BwdADRKF_2_62_7");
  }

  /**
   * Test index record.
   *
   * @throws IndexingException the indexing exception
   * @throws EuropeanaException the europeana exception
   */
  @Test
  void testIndexRecord() throws IndexingException, EuropeanaException {
    final String stringRdfRecord = IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_string.xml");

    indexer.indexRecord(stringRdfRecord);

    assertIndexedRecord("/50/_providedCHO_NL_BwdADRKF_2_126_10");
  }

  private void assertIndexedRecord(String expectedId) throws EuropeanaException {
    FullBean fullBean = recordDao.getFullBean(expectedId);
    assertNotNull(fullBean);
    assertEquals(expectedId, fullBean.getAbout());
  }

  /**
   * The type Mongo indexer local config test.
   */
  @Configuration
  static class MongoIndexerLocalConfigTest {

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
    MongoClient mongoClient(MongoProperties mongoProperties) throws Exception {
      return new MongoClientProvider<>(mongoProperties).createMongoClient();
    }

    /**
     * Mongo indexing settings mongo indexing settings.
     *
     * @param mongoProperties the mongo properties
     * @param mongoDatabase the mongo database
     * @param mongoRedirectDatabase the mongo redirect database
     * @return the mongo indexing settings
     * @throws SetupRelatedIndexingException the setup related indexing exception
     */
    @Bean
    MongoIndexingSettings mongoIndexingSettings(MongoProperties mongoProperties, @Value("${mongo.db}") String mongoDatabase,
        @Value("${mongo.redirect.db}") String mongoRedirectDatabase) throws SetupRelatedIndexingException {
      MongoIndexingSettings mongoIndexingSettings = new MongoIndexingSettings(mongoProperties);
      mongoIndexingSettings.setMongoDatabaseName(mongoDatabase);
      mongoIndexingSettings.setRecordRedirectDatabaseName(mongoRedirectDatabase);
      IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
          true,
          List.of(), true, true);
      mongoIndexingSettings.setIndexingProperties(indexingProperties);
      return mongoIndexingSettings;
    }

    /**
     * Record dao record dao.
     *
     * @param mongoClient the mongo client
     * @param settings the settings
     * @return the record dao
     */
    @Bean
    RecordDao recordDao(MongoClient mongoClient, MongoIndexingSettings settings) {
      return new RecordDao(mongoClient, settings.getMongoDatabaseName());
    }

    /**
     * Indexer mongo indexer.
     *
     * @param settings the settings
     * @return the mongo indexer
     * @throws SetupRelatedIndexingException the setup related indexing exception
     */
    @Bean
    MongoIndexer indexer(MongoIndexingSettings settings) throws SetupRelatedIndexingException {
      return new MongoIndexer(settings);
    }
  }
}
