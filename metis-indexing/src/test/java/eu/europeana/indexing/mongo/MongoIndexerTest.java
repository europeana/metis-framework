package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mongodb.client.MongoClient;
import eu.europeana.indexing.base.TestContainer;
import eu.europeana.indexing.base.TestContainerFactoryIT;
import eu.europeana.indexing.base.TestContainerType;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.mongo.MongoIndexerTest.MongoIndexerLocalConfigTest;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.schema.jibx.RDF;
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
@ContextConfiguration(classes = MongoIndexerLocalConfigTest.class)
class MongoIndexerTest {

  @Autowired
  private MongoIndexer indexer;

  @Autowired
  private MongoClient mongoClient;

  @DynamicPropertySource
  public static void dynamicProperties(DynamicPropertyRegistry registry) {
    TestContainer mongoContainerIT = TestContainerFactoryIT.getContainer(TestContainerType.MONGO);
    mongoContainerIT.dynamicProperties(registry);
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
  void testIndexRecord() {
  }

  @Configuration
  static class MongoIndexerLocalConfigTest {

    @Bean
    MongoProperties<SetupRelatedIndexingException> mongoProperties(@Value("${mongo.hosts}") String mongoHost,
        @Value("${mongo.port}") int mongoPort)
        throws URISyntaxException, SetupRelatedIndexingException {
      MongoProperties<SetupRelatedIndexingException> mongoProperties = new MongoProperties<>(SetupRelatedIndexingException::new);
      mongoProperties.setMongoHosts(new String[]{mongoHost}, new int[]{mongoPort});

      return mongoProperties;
    }

    @Bean
    MongoClient mongoClient(MongoProperties mongoProperties) throws Exception {
      return new MongoClientProvider<>(mongoProperties).createMongoClient();
    }

    @Bean
    MongoIndexer indexer(MongoProperties mongoProperties) throws SetupRelatedIndexingException {
      return new MongoIndexer(mongoProperties);
    }
  }
}
