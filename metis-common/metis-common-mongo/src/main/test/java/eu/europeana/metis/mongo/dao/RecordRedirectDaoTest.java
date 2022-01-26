package eu.europeana.metis.mongo.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.DatastoreImpl;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import eu.europeana.metis.mongo.model.RecordRedirect;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link RecordRedirectDao}
 *
 */
class RecordRedirectDaoTest {
  private final static String DATABASE_NAME = "dbTest";

  private static RecordRedirectDao recordRedirectDao;

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  @BeforeAll
  static void setup() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();
    final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    recordRedirectDao = new RecordRedirectDao(mongoClient,DATABASE_NAME);
  }

  @AfterAll
  static void tearDown() {
    embeddedLocalhostMongo.stop();
  }

  @Test
  void createAndGetById() {
    final RecordRedirect expectedRecordRedirect = getRecordRedirect();

    final String newId = recordRedirectDao.createUpdate(expectedRecordRedirect);
    RecordRedirect actual = recordRedirectDao.getById(expectedRecordRedirect.getId().toString());

    assertNotNull(newId);
    assertEquals(expectedRecordRedirect.getId(), actual.getId());
    assertEquals(expectedRecordRedirect.getOldId(), actual.getOldId());
    assertEquals(expectedRecordRedirect.getTimestamp(), actual.getTimestamp());
  }

  @Test
  void delete() {
    RecordRedirect recordRedirect = getRecordRedirect();
    String newId = recordRedirectDao.createUpdate(recordRedirect);
    recordRedirect.setNewId(newId);

    recordRedirectDao.delete(recordRedirect);
    RecordRedirect actual = recordRedirectDao.getById(recordRedirect.getId().toString());

    assertNull(actual);
  }

  @Test
  void getRecordRedirectsByOldId() {
    RecordRedirect recordRedirect = getRecordRedirect();
    String newId = recordRedirectDao.createUpdate(recordRedirect);
    recordRedirect.setNewId(newId);

    List<RecordRedirect> recordRedirectList = recordRedirectDao.getRecordRedirectsByOldId("61eec080f582833f364dad02");
    assertEquals(1, recordRedirectList.size());
  }

  @Test
  void getRecordRedirectsByNewId() {
    RecordRedirect recordRedirect = getRecordRedirect();
    recordRedirect.setNewId("61eec080f582833f364dad08");
    recordRedirectDao.createUpdate(recordRedirect);

    List<RecordRedirect> recordRedirectList = recordRedirectDao.getRecordRedirectsByNewId("61eec080f582833f364dad08");

    assertEquals(1, recordRedirectList.size());
    assertEquals(recordRedirect.getNewId(), recordRedirectList.get(0).getNewId());
  }

  @Test
  void getDatastore() {
    final Datastore datastore = recordRedirectDao.getDatastore();

    assertNotNull(datastore);
  }

  private static RecordRedirect getRecordRedirect() {
    final RecordRedirect recordRedirect = new RecordRedirect();
    recordRedirect.setId(new ObjectId("61eec080f582833f364dad05"));
    recordRedirect.setOldId(new ObjectId("61eec080f582833f364dad02").toString());
    recordRedirect.setTimestamp(Date.from(Instant.parse("2022-01-25T16:34:10.00Z")));
    return recordRedirect;
  }
}