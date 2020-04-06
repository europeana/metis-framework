package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.dereference.ContextualClass;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import redis.clients.jedis.Jedis;

/**
 * Created by ymamakis on 2/22/16.
 */
class MongoDereferencingManagementServiceTest {

  private MongoDereferencingManagementService service;
  private EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
  private Datastore vocDaoDatastore;

  @BeforeEach
  void prepare() {
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();

    RedisProvider redisProvider = Mockito.mock(RedisProvider.class);
    Jedis jedis = Mockito.mock(Jedis.class);
    CacheDao cacheDao = new CacheDao(redisProvider);
    MongoClient mongo = new MongoClient(mongoHost, mongoPort);

    VocabularyDao vocDao = new VocabularyDao(mongo, "voctest") {
      {
        vocDaoDatastore = this.getDatastore();
      }
    };
    service = new MongoDereferencingManagementService(vocDao, cacheDao);

    Mockito.when(redisProvider.getJedis()).thenReturn(jedis);
  }

  @Test
  void testGetAllVocabularies() {
    Vocabulary voc = new Vocabulary();
    voc.setIterations(0);
    voc.setName("testName");
    voc.setRules(Collections.singleton("testRules"));
    voc.setType(ContextualClass.AGENT);
    voc.setTypeRules(Collections.singleton("testTypeRules"));
    voc.setUri("testURI");
    voc.setXslt("testXSLT");
    vocDaoDatastore.save(voc);
    List<Vocabulary> retVoc = service.getAllVocabularies();
    assertEquals(1, retVoc.size());
  }

  @AfterEach
  void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
