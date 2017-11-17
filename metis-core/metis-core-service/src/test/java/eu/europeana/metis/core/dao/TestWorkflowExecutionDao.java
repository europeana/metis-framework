package eu.europeana.metis.core.dao;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
public class TestWorkflowExecutionDao {

  private static UserWorkflowExecutionDao userWorkflowExecutionDao;
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

    userWorkflowExecutionDao = new UserWorkflowExecutionDao(provider);
    userWorkflowExecutionDao.setUserWorkflowExecutionsPerRequest(5);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(WorkflowExecution.class));
  }

  @Test
  public void createUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void updateUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(workflowExecution);
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date updatedDate = new Date();
    workflowExecution.setUpdatedDate(updatedDate);
    String objectId = userWorkflowExecutionDao.update(workflowExecution);
    Assert.assertNotNull(objectId);
    WorkflowExecution updatedWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
    Assert.assertEquals(WorkflowStatus.RUNNING, updatedWorkflowExecution.getWorkflowStatus());
    Assert.assertTrue(updatedDate.compareTo(updatedWorkflowExecution.getUpdatedDate()) == 0);
  }

  @Test
  public void updateWorkflowPlugins() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Assert.assertEquals(PluginStatus.INQUEUE,
        workflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    workflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    WorkflowExecution updatedWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
    Assert.assertEquals(PluginStatus.RUNNING,
        updatedWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
  }

  @Test
  public void updateMonitorInformation() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Date createdDate = new Date();
    workflowExecution.setCreatedDate(createdDate);
    Assert.assertEquals(PluginStatus.INQUEUE,
        workflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date startedDate = new Date();
    workflowExecution.setStartedDate(startedDate);
    workflowExecution.setUpdatedDate(startedDate);
    workflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    Date pluginUpdatedDate = new Date();
    workflowExecution.getMetisPlugins().get(0).setUpdatedDate(pluginUpdatedDate);
    userWorkflowExecutionDao.updateMonitorInformation(workflowExecution);
    WorkflowExecution updatedWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
    Assert.assertEquals(WorkflowStatus.RUNNING, updatedWorkflowExecution.getWorkflowStatus());
    Assert.assertTrue(createdDate.compareTo(updatedWorkflowExecution.getCreatedDate()) == 0);
    Assert.assertTrue(startedDate.compareTo(updatedWorkflowExecution.getStartedDate()) == 0);
    Assert.assertTrue(startedDate.compareTo(updatedWorkflowExecution.getUpdatedDate()) == 0);
    Assert.assertEquals(PluginStatus.RUNNING,
        updatedWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
    Assert.assertTrue(pluginUpdatedDate
        .compareTo(updatedWorkflowExecution.getMetisPlugins().get(0).getUpdatedDate()) == 0);
  }

  @Test
  public void testSetCancellingState() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    userWorkflowExecutionDao.setCancellingState(workflowExecution);
    WorkflowExecution cancellingWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    Assert.assertTrue(cancellingWorkflowExecution.isCancelling());
  }

  @Test
  public void getById() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Assert.assertFalse(workflowExecution.isCancelling());
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    Assert.assertEquals(workflowExecution.getCreatedDate(),
        retrievedWorkflowExecution.getCreatedDate());
    Assert.assertEquals(workflowExecution.getDatasetName(),
        retrievedWorkflowExecution.getDatasetName());
    Assert.assertEquals(workflowExecution.getWorkflowOwner(),
        retrievedWorkflowExecution.getWorkflowOwner());
    Assert.assertEquals(workflowExecution.getWorkflowName(),
        retrievedWorkflowExecution.getWorkflowName());
    Assert.assertEquals(workflowExecution.getWorkflowPriority(),
        retrievedWorkflowExecution.getWorkflowPriority());
    Assert.assertFalse(retrievedWorkflowExecution.isCancelling());
    Assert.assertEquals(workflowExecution.getMetisPlugins().get(0).getPluginType(),
        retrievedWorkflowExecution.getMetisPlugins().get(0).getPluginType());
  }

  @Test
  public void delete() {
    Assert.assertFalse(userWorkflowExecutionDao.delete(null));
  }

  @Test
  public void getRunningOrInQueueExecution() {
    WorkflowExecution workflowExecutionRunning = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecutionRunning.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.create(workflowExecutionRunning);
    WorkflowExecution runningOrInQueueExecution = userWorkflowExecutionDao
        .getRunningOrInQueueExecution(workflowExecutionRunning.getDatasetName());
    Assert.assertEquals(WorkflowStatus.RUNNING, runningOrInQueueExecution.getWorkflowStatus());
  }

  @Test
  public void exists() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(userWorkflowExecutionDao.exists(workflowExecution));
  }

  @Test
  public void existsAndNotCompleted() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertEquals(objectId, userWorkflowExecutionDao
        .existsAndNotCompleted(workflowExecution.getDatasetName()));
  }

  @Test
  public void existsAndNotCompletedReturnNull() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertNull(
        userWorkflowExecutionDao.existsAndNotCompleted(workflowExecution.getDatasetName()));
  }

  @Test
  public void getRunningUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    WorkflowExecution runningWorkflowExecution = userWorkflowExecutionDao
        .getRunningUserWorkflowExecution(workflowExecution.getDatasetName());
    Assert.assertEquals(objectId, runningWorkflowExecution.getId().toString());
  }

  @Test
  public void getAllUserWorkflowExecutions() {
    int userWorkflowExecutionsToCreate =
        userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest() + 1;
    for (int i = 0; i < userWorkflowExecutionsToCreate; i++) {
      WorkflowExecution workflowExecution = TestObjectFactory
          .createUserWorkflowExecutionObject();
      userWorkflowExecutionDao.create(workflowExecution);
    }
    String nextPage = null;
    int allUserWorkflowsExecutionsCount = 0;
    do {
      ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(userWorkflowExecutionDao
              .getAllUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
                  TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME,
                  WorkflowStatus.INQUEUE, nextPage),
          userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest());
      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != null);

    Assert.assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

  @Test
  public void getAllUserWorkflowExecutionsByWorkflowStatus() {
    int userWorkflowExecutionsToCreate =
        userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest() + 1;
    for (int i = 0; i < userWorkflowExecutionsToCreate; i++) {
      WorkflowExecution workflowExecution = TestObjectFactory
          .createUserWorkflowExecutionObject();
      userWorkflowExecutionDao.create(workflowExecution);
    }
    String nextPage = null;
    int allUserWorkflowsExecutionsCount = 0;
    do {
      ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(userWorkflowExecutionDao
              .getAllUserWorkflowExecutions(WorkflowStatus.INQUEUE, nextPage),
          userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest());
      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != null);

    Assert.assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

  @Test
  public void isCancelled() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(userWorkflowExecutionDao.isCancelled(new ObjectId(objectId)));
  }

  @Test
  public void isCancelling() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setCancelling(true);
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(userWorkflowExecutionDao.isCancelling(new ObjectId(objectId)));
  }

  @Test
  public void isExecutionActive() {
    Date beforeDate = new Date();
    Date afterDate = new Date(beforeDate.getTime() + 1000);
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(afterDate);
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    retrievedWorkflowExecution.setUpdatedDate(beforeDate);
    Assert
        .assertTrue(userWorkflowExecutionDao.isExecutionActive(retrievedWorkflowExecution, 0));
  }

  @Test
  public void isExecutionActiveInterrupted() throws InterruptedException {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Thread t = new Thread(() -> Assert
        .assertTrue(userWorkflowExecutionDao.isExecutionActive(workflowExecution, 10)));
    t.start();
    Awaitility.await().atMost(Duration.TWO_SECONDS).until(() -> untilThreadIsSleeping(t));
    t.interrupt();
  }

  @Test
  public void isExecutionActiveFalse() {
    Date updatedDate = new Date();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(updatedDate);
    String objectId = userWorkflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    Assert
        .assertFalse(userWorkflowExecutionDao.isExecutionActive(retrievedWorkflowExecution, 0));
  }

  @Test
  public void removeActiveExecutionsFromList() {
    Date updatedDate = new Date();
    Date beforeDate = updatedDate;
    Date afterDate = new Date(beforeDate.getTime() + 1000);
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(updatedDate);
    userWorkflowExecutionDao.create(workflowExecution);
    WorkflowExecution workflowExecutionUpdated = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecutionUpdated.setUpdatedDate(afterDate);
    String objectId = userWorkflowExecutionDao.create(workflowExecutionUpdated);
    workflowExecutionUpdated.setUpdatedDate(beforeDate);

    List<WorkflowExecution> workflowExecutions = new ArrayList<>();
    workflowExecutions.add(workflowExecution);
    workflowExecutions.add(workflowExecutionUpdated);
    Assert.assertEquals(2, workflowExecutions.size());
    userWorkflowExecutionDao.removeActiveExecutionsFromList(workflowExecutions, 0);
    Assert.assertEquals(1, workflowExecutions.size());
  }

  @Test
  public void removeActiveExecutionsFromListInterrupted() throws InterruptedException {
    Thread t = new Thread(() -> {
      Date beforeDate = new Date();
      Date afterDate = new Date(beforeDate.getTime() + 1000);
      WorkflowExecution workflowExecution = TestObjectFactory
          .createUserWorkflowExecutionObject();
      workflowExecution.setUpdatedDate(new Date());
      workflowExecution.setUpdatedDate(afterDate);
      userWorkflowExecutionDao.create(workflowExecution);
      workflowExecution.setUpdatedDate(beforeDate);
      List<WorkflowExecution> workflowExecutions = new ArrayList<>();
      workflowExecutions.add(workflowExecution);
      userWorkflowExecutionDao.removeActiveExecutionsFromList(workflowExecutions, 10);
      Assert.assertEquals(1, workflowExecutions.size());
    });
    t.start();
    Awaitility.await().atMost(Duration.TWO_SECONDS).until(() -> untilThreadIsSleeping(t));
    t.interrupt();
  }

  @Test
  public void deleteAllByDatasetName() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(
        userWorkflowExecutionDao.deleteAllByDatasetName(workflowExecution.getDatasetName()));
  }

  @Test
  public void updateAllDatasetNames() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(workflowExecution);
    String updatedDatasetName = "updatedDatasetName";
    userWorkflowExecutionDao
        .updateAllDatasetNames(workflowExecution.getDatasetName(), updatedDatasetName);
    workflowExecution.setDatasetName(updatedDatasetName);
    Assert.assertTrue(userWorkflowExecutionDao.exists(workflowExecution));
  }

  private void untilThreadIsSleeping(Thread t) {
    Assert.assertEquals("java.lang.Thread", t.getStackTrace()[0].getClassName());
    Assert.assertEquals("sleep", t.getStackTrace()[0].getMethodName());
  }

}
