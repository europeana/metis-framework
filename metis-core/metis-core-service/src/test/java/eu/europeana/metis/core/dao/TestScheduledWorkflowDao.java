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
    scheduledWorkflowDao.setScheduledUserWorkflowPerRequest(5);
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
        .createScheduledUserWorkflowObject();
    String objectId = scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void updateScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
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
        .createScheduledUserWorkflowObject();
    String objectId = scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getById(objectId);
    Assert.assertEquals(scheduledWorkflow.getDatasetName(),
        retrievedScheduledWorkflow.getDatasetName());
    Assert.assertEquals(scheduledWorkflow.getWorkflowOwner(),
        retrievedScheduledWorkflow.getWorkflowOwner());
    Assert.assertEquals(scheduledWorkflow.getWorkflowName(),
        retrievedScheduledWorkflow.getWorkflowName());
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
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledUserWorkflow(scheduledWorkflow.getDatasetName(),
            scheduledWorkflow.getWorkflowOwner(), scheduledWorkflow.getWorkflowName());
    Assert.assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    Assert.assertTrue(scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void getScheduledUserWorkflowByDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledUserWorkflowByDatasetName(scheduledWorkflow.getDatasetName());
    Assert.assertEquals(scheduledWorkflow.getWorkflowOwner(),
        retrievedScheduledWorkflow.getWorkflowOwner());
    Assert.assertEquals(scheduledWorkflow.getWorkflowName(),
        retrievedScheduledWorkflow.getWorkflowName());
    Assert.assertEquals(scheduledWorkflow.getScheduleFrequence(),
        retrievedScheduledWorkflow.getScheduleFrequence());
    Assert.assertTrue(scheduledWorkflow.getPointerDate()
        .compareTo(retrievedScheduledWorkflow.getPointerDate()) == 0);
  }

  @Test
  public void exists() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertTrue(scheduledWorkflowDao.exists(scheduledWorkflow));
  }

  @Test
  public void existsForDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertNotNull(
        scheduledWorkflowDao.existsForDatasetName(scheduledWorkflow.getDatasetName()));
  }

  @Test
  public void deleteScheduledUserWorkflow() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertTrue(
        scheduledWorkflowDao.deleteScheduledUserWorkflow(scheduledWorkflow.getDatasetName()));
  }

  @Test
  public void deleteAllByDatasetName() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    Assert.assertTrue(
        scheduledWorkflowDao.deleteAllByDatasetName(scheduledWorkflow.getDatasetName()));
  }

  @Test
  public void updateAllDatasetNames() {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflowDao.create(scheduledWorkflow);
    String newDatasetName = "newDatasetName";
    scheduledWorkflowDao
        .updateAllDatasetNames(scheduledWorkflow.getDatasetName(), newDatasetName);
    ScheduledWorkflow retrievedScheduledWorkflow = scheduledWorkflowDao
        .getScheduledUserWorkflowByDatasetName(newDatasetName);
    Assert.assertTrue(retrievedScheduledWorkflow.getDatasetName().equals(newDatasetName));
  }

  @Test
  public void getAllScheduledUserWorkflows() {
    int scheduledUserWorkflowToCreate =
        scheduledWorkflowDao.getScheduledUserWorkflowPerRequest() + 1;
    for (int i = 0; i < scheduledUserWorkflowToCreate; i++) {
      ScheduledWorkflow scheduledWorkflow = TestObjectFactory
          .createScheduledUserWorkflowObject();
      scheduledWorkflow.setDatasetName(String.format("%s%s", TestObjectFactory.DATASETNAME, i));
      scheduledWorkflowDao.create(scheduledWorkflow);
    }
    String nextPage = null;
    int allScheduledUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper.setResultsAndLastPage(
          scheduledWorkflowDao.getAllScheduledUserWorkflows(ScheduleFrequence.ONCE, nextPage),
          scheduledWorkflowDao.getScheduledUserWorkflowPerRequest());
      allScheduledUserWorkflowsCount += scheduledUserWorkflowResponseListWrapper.getListSize();
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != null);

    Assert.assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

  @Test
  public void getAllScheduledUserWorkflowsByDateRangeONCE()
  {
    int minutesRange = 10;
    LocalDateTime lowerBound = LocalDateTime.now();
    LocalDateTime upperBound = lowerBound.plusMinutes(minutesRange);

    int scheduledUserWorkflowToCreate =
        scheduledWorkflowDao.getScheduledUserWorkflowPerRequest() + 1;
    for (int i = 0; i < scheduledUserWorkflowToCreate; i++) {
      ScheduledWorkflow scheduledWorkflow = TestObjectFactory
          .createScheduledUserWorkflowObject();
      scheduledWorkflow.setDatasetName(String.format("%s%s", TestObjectFactory.DATASETNAME, i));
      int plusMinutes = ThreadLocalRandom.current().nextInt(1, minutesRange);
      Date pointerDate = Date
          .from(lowerBound.plusMinutes(plusMinutes).atZone(ZoneId.systemDefault()).toInstant());
      scheduledWorkflow.setPointerDate(pointerDate);
      scheduledWorkflowDao.create(scheduledWorkflow);
    }
    String nextPage = null;
    int allScheduledUserWorkflowsCount = 0;
    do {
      ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper.setResultsAndLastPage(
          scheduledWorkflowDao
              .getAllScheduledUserWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage),
          scheduledWorkflowDao.getScheduledUserWorkflowPerRequest());
      allScheduledUserWorkflowsCount += scheduledUserWorkflowResponseListWrapper.getListSize();
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != null);

    Assert.assertEquals(scheduledUserWorkflowToCreate, allScheduledUserWorkflowsCount);
  }

}
