package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import eu.europeana.metis.dereference.ContextualClass;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    MongoClient mongo = new MongoClient(mongoHost, mongoPort);

    VocabularyDao vocDao = new VocabularyDao(mongo, "voctest") {
      {
        vocDaoDatastore = this.getDatastore();
      }
    };
    EntityDao<OriginalEntity> originalEntityDao = mock(EntityDao.class);
    EntityDao<ProcessedEntity> processedEntityDao = mock(EntityDao.class);
    service = new MongoDereferencingManagementService(vocDao, originalEntityDao, processedEntityDao);
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
