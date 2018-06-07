package eu.europeana.metis.core.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
public class TestScheduledWorkflowDao {

  private static ScheduledWorkflowDao scheduledWorkflowDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeClass
  public static void prepare() throws IOException {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    provider = new MorphiaDatastoreProvider(mongoClient, "test");

    scheduledWorkflowDao = new ScheduledWorkflowDao(provider);
    scheduledWorkflowDao.setScheduledWorkflowPerRequest(5);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(ScheduledWorkflow.class));
  }

  @Test
  public void createScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    String objectId = scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void updateScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Date updatedPointerDate = new Date();
    scheduledWorkflow.setPointerDate(updatedPointerDate);
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.MONTHLY);
    String objectId = scheduledWorkflowDao.update(scheduledWorkflow);
    ScheduledWorkflow updatedScheduledWorkflow = scheduledWorkflowDao.getById(objectId);
    Assert.assertEquals(ScheduleFrequence.MONTHLY,
        updatedScheduledWorkflow.getScheduleFrequence());
    Assert.assertTrue(
        updatedPointerDate.compareTo(updatedScheduledWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void getById() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    String objectId = scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getById(objectId);
    Assert.assertEquals(scheduledWorkflow.getDatasetId(),
        retrievedScheduledWorkflow.getDatasetId());
    Assert.assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    Assert.assertEquals(scheduledWorkflow.getWorkflowPriority(),
        retrievedScheduledWorkflow.getWorkflowPriority());
    Assert.assertTrue(scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void delete() {
    Assert.assertFalse(scheduledWorkflowDao.delete(null));
  }

  @Test
  public void getScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledWorkflow(scheduledWorkflow.getDatasetId());
    Assert.assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    Assert.assertTrue(scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void getScheduledUserWorkflowByDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledWorkflowByDatasetId(scheduledWorkflow.getDatasetId());
    Assert.assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    Assert.assertTrue(scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void exists() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertTrue(scheduledWorkflowDao.exists(scheduledWorkflow));
  }

  @Test
  public void existsForDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertNotNull(
        scheduledWorkflowDao.existsForDatasetId(scheduledWorkflow.getDatasetId()));
  }

  @Test
  public void deleteScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertTrue(
        scheduledWorkflowDao.deleteScheduledWorkflow(scheduledWorkflow.getDatasetId()));
  }

  @Test
  public void deleteAllByDatasetId() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertTrue(
        scheduledWorkflowDao.deleteAllByDatasetId(scheduledWorkflow.getDatasetId()));
  }

  @Test
  public void getAllScheduledUserWorkflows() {
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

    Assert.assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

  @Test
  public void getAllScheduledUserWorkflowsByDateRangeONCE()
  {
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

    Assert.assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

}
