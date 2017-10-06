package eu.europeana.metis.core.test.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dao.ScheduledUserWorkflowDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
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
public class TestScheduledUserWorkflowDao {

  private static ScheduledUserWorkflowDao scheduledUserWorkflowDao;
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

    scheduledUserWorkflowDao = new ScheduledUserWorkflowDao(provider);
    scheduledUserWorkflowDao.setScheduledUserWorkflowPerRequest(5);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(ScheduledUserWorkflow.class));
  }

  @Test
  public void testCreateScheduledUserWorkflow() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    String objectId = scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void testUpdateScheduledUserWorkflow() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    Date updatedPointerDate = new Date();
    scheduledUserWorkflow.setPointerDate(updatedPointerDate);
    scheduledUserWorkflow.setScheduleFrequence(ScheduleFrequence.MONTHLY);
    String objectId = scheduledUserWorkflowDao.update(scheduledUserWorkflow);
    ScheduledUserWorkflow updatedScheduledUserWorkflow = scheduledUserWorkflowDao.getById(objectId);
    Assert.assertEquals(ScheduleFrequence.MONTHLY,
        updatedScheduledUserWorkflow.getScheduleFrequence());
    Assert.assertTrue(
        updatedPointerDate.compareTo(updatedScheduledUserWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void testGetById() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    String objectId = scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    ScheduledUserWorkflow retrievedScheduledUserWorkflow = scheduledUserWorkflowDao
        .getById(objectId);
    Assert.assertEquals(scheduledUserWorkflow.getDatasetName(),
        retrievedScheduledUserWorkflow.getDatasetName());
    Assert.assertEquals(scheduledUserWorkflow.getWorkflowOwner(),
        retrievedScheduledUserWorkflow.getWorkflowOwner());
    Assert.assertEquals(scheduledUserWorkflow.getWorkflowName(),
        retrievedScheduledUserWorkflow.getWorkflowName());
    Assert.assertEquals(scheduledUserWorkflow.getScheduleFrequence(),
        retrievedScheduledUserWorkflow.getScheduleFrequence());
    Assert.assertEquals(scheduledUserWorkflow.getWorkflowPriority(),
        retrievedScheduledUserWorkflow.getWorkflowPriority());
    Assert.assertTrue(scheduledUserWorkflow.getPointerDate()
        .compareTo(retrievedScheduledUserWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void testDelete() {
    Assert.assertFalse(scheduledUserWorkflowDao.delete(null));
  }

  @Test
  public void testGetScheduledUserWorkflow() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    ScheduledUserWorkflow retrievedScheduledUserWorkflow = scheduledUserWorkflowDao
        .getScheduledUserWorkflow(scheduledUserWorkflow.getDatasetName(),
            scheduledUserWorkflow.getWorkflowOwner(), scheduledUserWorkflow.getWorkflowName());
    Assert.assertEquals(scheduledUserWorkflow.getScheduleFrequence(),
        retrievedScheduledUserWorkflow.getScheduleFrequence());
    Assert.assertTrue(scheduledUserWorkflow.getPointerDate()
        .compareTo(retrievedScheduledUserWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void testGetScheduledUserWorkflowByDatasetName() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    ScheduledUserWorkflow retrievedScheduledUserWorkflow = scheduledUserWorkflowDao
        .getScheduledUserWorkflowByDatasetName(scheduledUserWorkflow.getDatasetName());
    Assert.assertEquals(scheduledUserWorkflow.getWorkflowOwner(),
        retrievedScheduledUserWorkflow.getWorkflowOwner());
    Assert.assertEquals(scheduledUserWorkflow.getWorkflowName(),
        retrievedScheduledUserWorkflow.getWorkflowName());
    Assert.assertEquals(scheduledUserWorkflow.getScheduleFrequence(),
        retrievedScheduledUserWorkflow.getScheduleFrequence());
    Assert.assertTrue(scheduledUserWorkflow.getPointerDate()
        .compareTo(retrievedScheduledUserWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void testExists() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    Assert.assertTrue(scheduledUserWorkflowDao.exists(scheduledUserWorkflow));
  }

  @Test
  public void testExistsForDatasetName() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    Assert.assertNotNull(
        scheduledUserWorkflowDao.existsForDatasetName(scheduledUserWorkflow.getDatasetName()));
  }

  @Test
  public void testDeleteScheduledUserWorkflow() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    Assert.assertTrue(
        scheduledUserWorkflowDao.deleteScheduledUserWorkflow(scheduledUserWorkflow.getDatasetName(),
            scheduledUserWorkflow.getWorkflowOwner(), scheduledUserWorkflow.getWorkflowName()));
  }

  @Test
  public void testDeleteAllByDatasetName() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    Assert.assertTrue(
        scheduledUserWorkflowDao.deleteAllByDatasetName(scheduledUserWorkflow.getDatasetName()));
  }

  @Test
  public void testUpdateAllDatasetNames() {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    String newDatasetName = "newDatasetName";
    scheduledUserWorkflowDao
        .updateAllDatasetNames(scheduledUserWorkflow.getDatasetName(), newDatasetName);
    ScheduledUserWorkflow retrievedScheduledUserWorkflow = scheduledUserWorkflowDao
        .getScheduledUserWorkflowByDatasetName(newDatasetName);
    Assert.assertTrue(retrievedScheduledUserWorkflow.getDatasetName().equals(newDatasetName));
  }

  @Test
  public void testGetAllScheduledUserWorkflows() {
    int scheduledUserWorkflowToCreate =
        scheduledUserWorkflowDao.getScheduledUserWorkflowPerRequest() + 1;
    for (int i = 0; i < scheduledUserWorkflowToCreate; i++) {
      ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
          .createScheduledUserWorkflowObject();
      scheduledUserWorkflow.setDatasetName(String.format("%s%s", TestObjectFactory.DATASETNAME, i));
      scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    }
    String nextPage = null;
    int allScheduledUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<ScheduledUserWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper.setResultsAndLastPage(
          scheduledUserWorkflowDao.getAllScheduledUserWorkflows(ScheduleFrequence.ONCE, nextPage),
          scheduledUserWorkflowDao.getScheduledUserWorkflowPerRequest());
      allScheduledUserWorkflowsCount += scheduledUserWorkflowResponseListWrapper.getListSize();
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != null);

    Assert.assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

  @Test
  public void testGetAllScheduledUserWorkflowsByDateRangeONCE()
  {
    int minutesRange = 10;
    LocalDateTime lowerBound = LocalDateTime.now();
    LocalDateTime upperBound = lowerBound.plusMinutes(minutesRange);

    int scheduledUserWorkflowToCreate =
        scheduledUserWorkflowDao.getScheduledUserWorkflowPerRequest() + 1;
    for (int i = 0; i < scheduledUserWorkflowToCreate; i++) {
      ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
          .createScheduledUserWorkflowObject();
      scheduledUserWorkflow.setDatasetName(String.format("%s%s", TestObjectFactory.DATASETNAME, i));
      int plusMinutes = ThreadLocalRandom.current().nextInt(1, minutesRange);
      Date pointerDate = Date
          .from(lowerBound.plusMinutes(plusMinutes).atZone(ZoneId.systemDefault()).toInstant());
      scheduledUserWorkflow.setPointerDate(pointerDate);
      scheduledUserWorkflowDao.create(scheduledUserWorkflow);
    }
    String nextPage = null;
    int allScheduledUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<ScheduledUserWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper.setResultsAndLastPage(
          scheduledUserWorkflowDao.getAllScheduledUserWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage),
          scheduledUserWorkflowDao.getScheduledUserWorkflowPerRequest());
      allScheduledUserWorkflowsCount += scheduledUserWorkflowResponseListWrapper.getListSize();
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != null);

    Assert.assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

}
