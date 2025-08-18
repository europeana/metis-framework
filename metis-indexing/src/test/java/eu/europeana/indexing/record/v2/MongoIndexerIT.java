package eu.europeana.indexing.record.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mongodb.client.MongoClient;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.indexing.IndexerPreprocessor;
import eu.europeana.indexing.IndexingProperties;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.base.TestContainer;
import eu.europeana.indexing.base.TestContainerFactoryIT;
import eu.europeana.indexing.base.TestContainerType;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.MongoIndexerIT.MongoIndexerLocalConfigTest;
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
class MongoIndexerIT {

  @Autowired
  private RecordPersistenceV2 indexer;

  @Autowired
  private RecordDao recordDao;

  private static final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
      true, List.of(), true, true);

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
  void NullPointerExceptionTest() {
    NullPointerException expected = assertThrows(NullPointerException.class, () -> indexer.saveRecord((RDF) null));
    assertEquals("record is null", expected.getMessage());
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
    IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);
    indexer.saveRecord(inputRdf);

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

    indexer.saveRecord(stringRdfRecord);

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
     * Record dao record dao.
     *
     * @param mongoClient the mongo client
     * @param mongoDatabase the mongo database
     * @return the record dao
     */
    @Bean
    RecordDao recordDao(MongoClient mongoClient, @Value("${mongo.db}") String mongoDatabase) {
      return new RecordDao(mongoClient, mongoDatabase);
    }

    /**
     * Indexer mongo indexer.
     *
     * @param recordDao the RecordDao
     * @return the mongo indexer
     */
    @Bean
    RecordPersistenceV2 indexer(RecordDao recordDao) {
      return new RecordPersistenceV2(recordDao);
    }
  }
}
