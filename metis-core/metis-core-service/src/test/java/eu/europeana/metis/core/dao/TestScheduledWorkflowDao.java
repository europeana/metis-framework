package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
class TestScheduledWorkflowDao {

  private static ScheduledWorkflowDao scheduledWorkflowDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProviderImpl provider;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = new MorphiaDatastoreProviderImpl(mongoClient, "test");

    scheduledWorkflowDao = new ScheduledWorkflowDao(provider);
    scheduledWorkflowDao.setScheduledWorkflowPerRequest(5);
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.find(ScheduledWorkflow.class).delete(new DeleteOptions().multi(true));
  }

  @Test
  void createScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    String objectId = scheduledWorkflowDao.create(scheduledWorkflow);
    assertNotNull(objectId);
  }

  @Test
  void updateScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Date updatedPointerDate = new Date();
    scheduledWorkflow.setPointerDate(updatedPointerDate);
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.MONTHLY);
    String objectId = scheduledWorkflowDao.update(scheduledWorkflow);
    ScheduledWorkflow updatedScheduledWorkflow = scheduledWorkflowDao.getById(objectId);
    assertEquals(ScheduleFrequence.MONTHLY,
        updatedScheduledWorkflow.getScheduleFrequence());
    assertEquals(0, updatedPointerDate.compareTo(updatedScheduledWorkflow.getPointerDate()));
  }

  @Test
  void getById() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    String objectId = scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getById(objectId);
    assertEquals(scheduledWorkflow.getDatasetId(),
        retrievedScheduledWorkflow.getDatasetId());
    assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    assertEquals(scheduledWorkflow.getWorkflowPriority(),
        retrievedScheduledWorkflow.getWorkflowPriority());
    assertEquals(0, scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()));
  }

  @Test
  void delete() {
    assertFalse(scheduledWorkflowDao.delete(null));
  }

  @Test
  void getScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledWorkflow(scheduledWorkflow.getDatasetId());
    assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    assertEquals(0, scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()));
  }

  @Test
  void getScheduledUserWorkflowByDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledWorkflowByDatasetId(scheduledWorkflow.getDatasetId());
    assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    assertEquals(0, scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()));
  }

  @Test
  void exists() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    assertTrue(scheduledWorkflowDao.exists(scheduledWorkflow));
  }

  @Test
  void existsForDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    assertNotNull(
        scheduledWorkflowDao.existsForDatasetId(scheduledWorkflow.getDatasetId()));
  }

  @Test
  void deleteScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    assertTrue(
        scheduledWorkflowDao.deleteScheduledWorkflow(scheduledWorkflow.getDatasetId()));
  }

  @Test
  void deleteAllByDatasetId() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    assertTrue(
        scheduledWorkflowDao.deleteAllByDatasetId(scheduledWorkflow.getDatasetId()));
  }

  @Test
  void getAllScheduledUserWorkflows() {
    int scheduledUserWorkflowToCreate =
        scheduledWorkflowDao.getScheduledWorkflowPerRequest() + 1;
    for (int i = 0; i < scheduledUserWorkflowToCreate; i++) {
      ScheduledWorkflow scheduledWorkflow = TestObjectFactory
          .createScheduledWorkflowObject();
      scheduledWorkflow.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + i));
      scheduledWorkflowDao.create(scheduledWorkflow);
    }
    int nextPage = 0;
    int allScheduledUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper.setResultsAndLastPage(
          scheduledWorkflowDao.getAllScheduledWorkflows(ScheduleFrequence.ONCE, nextPage),
          scheduledWorkflowDao.getScheduledWorkflowPerRequest(), nextPage);
      allScheduledUserWorkflowsCount += scheduledUserWorkflowResponseListWrapper.getListSize();
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

  @Test
  void getAllScheduledUserWorkflowsByDateRangeONCE() {
    int minutesRange = 10;
    LocalDateTime lowerBound = LocalDateTime.now();
    LocalDateTime upperBound = lowerBound.plusMinutes(minutesRange);

    int scheduledUserWorkflowToCreate =
        scheduledWorkflowDao.getScheduledWorkflowPerRequest() + 1;
    for (int i = 0; i < scheduledUserWorkflowToCreate; i++) {
      ScheduledWorkflow scheduledWorkflow = TestObjectFactory
          .createScheduledWorkflowObject();
      scheduledWorkflow.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + i));
      int plusMinutes = ThreadLocalRandom.current().nextInt(1, minutesRange);
      Date pointerDate = Date
          .from(lowerBound.plusMinutes(plusMinutes).atZone(ZoneId.systemDefault()).toInstant());
      scheduledWorkflow.setPointerDate(pointerDate);
      scheduledWorkflowDao.create(scheduledWorkflow);
    }
    int nextPage = 0;
    int allScheduledUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper.setResultsAndLastPage(
          scheduledWorkflowDao
              .getAllScheduledWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage),
          scheduledWorkflowDao.getScheduledWorkflowPerRequest(), nextPage);
      allScheduledUserWorkflowsCount += scheduledUserWorkflowResponseListWrapper.getListSize();
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

}
