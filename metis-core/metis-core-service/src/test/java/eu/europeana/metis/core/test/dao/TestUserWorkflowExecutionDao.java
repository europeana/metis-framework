package eu.europeana.metis.core.test.dao;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
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
public class TestUserWorkflowExecutionDao {

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
    datastore.delete(datastore.createQuery(UserWorkflowExecution.class));
  }

  @Test
  public void createUserWorkflowExecution() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void updateUserWorkflowExecution() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date updatedDate = new Date();
    userWorkflowExecution.setUpdatedDate(updatedDate);
    String objectId = userWorkflowExecutionDao.update(userWorkflowExecution);
    Assert.assertNotNull(objectId);
    UserWorkflowExecution updatedUserWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
    Assert.assertEquals(WorkflowStatus.RUNNING, updatedUserWorkflowExecution.getWorkflowStatus());
    Assert.assertTrue(updatedDate.compareTo(updatedUserWorkflowExecution.getUpdatedDate()) == 0);
  }

  @Test
  public void updateWorkflowPlugins() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Assert.assertEquals(PluginStatus.INQUEUE,
        userWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.updateWorkflowPlugins(userWorkflowExecution);
    UserWorkflowExecution updatedUserWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
    Assert.assertEquals(PluginStatus.RUNNING,
        updatedUserWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
  }

  @Test
  public void updateMonitorInformation() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Date createdDate = new Date();
    userWorkflowExecution.setCreatedDate(createdDate);
    Assert.assertEquals(PluginStatus.INQUEUE,
        userWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date startedDate = new Date();
    userWorkflowExecution.setStartedDate(startedDate);
    userWorkflowExecution.setUpdatedDate(startedDate);
    userWorkflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    Date pluginUpdatedDate = new Date();
    userWorkflowExecution.getMetisPlugins().get(0).setUpdatedDate(pluginUpdatedDate);
    userWorkflowExecutionDao.updateMonitorInformation(userWorkflowExecution);
    UserWorkflowExecution updatedUserWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
    Assert.assertEquals(WorkflowStatus.RUNNING, updatedUserWorkflowExecution.getWorkflowStatus());
    Assert.assertTrue(createdDate.compareTo(updatedUserWorkflowExecution.getCreatedDate()) == 0);
    Assert.assertTrue(startedDate.compareTo(updatedUserWorkflowExecution.getStartedDate()) == 0);
    Assert.assertTrue(startedDate.compareTo(updatedUserWorkflowExecution.getUpdatedDate()) == 0);
    Assert.assertEquals(PluginStatus.RUNNING,
        updatedUserWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
    Assert.assertTrue(pluginUpdatedDate
        .compareTo(updatedUserWorkflowExecution.getMetisPlugins().get(0).getUpdatedDate()) == 0);
  }

  @Test
  public void testSetCancellingState() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    userWorkflowExecutionDao.setCancellingState(userWorkflowExecution);
    UserWorkflowExecution cancellingUserWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    Assert.assertTrue(cancellingUserWorkflowExecution.isCancelling());
  }

  @Test
  public void getById() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Assert.assertFalse(userWorkflowExecution.isCancelling());
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    UserWorkflowExecution retrievedUserWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    Assert.assertEquals(userWorkflowExecution.getCreatedDate(),
        retrievedUserWorkflowExecution.getCreatedDate());
    Assert.assertEquals(userWorkflowExecution.getDatasetName(),
        retrievedUserWorkflowExecution.getDatasetName());
    Assert.assertEquals(userWorkflowExecution.getWorkflowOwner(),
        retrievedUserWorkflowExecution.getWorkflowOwner());
    Assert.assertEquals(userWorkflowExecution.getWorkflowName(),
        retrievedUserWorkflowExecution.getWorkflowName());
    Assert.assertEquals(userWorkflowExecution.getWorkflowPriority(),
        retrievedUserWorkflowExecution.getWorkflowPriority());
    Assert.assertFalse(retrievedUserWorkflowExecution.isCancelling());
    Assert.assertEquals(userWorkflowExecution.getMetisPlugins().get(0).getPluginType(),
        retrievedUserWorkflowExecution.getMetisPlugins().get(0).getPluginType());
  }

  @Test
  public void delete() {
    Assert.assertFalse(userWorkflowExecutionDao.delete(null));
  }

  @Test
  public void getRunningOrInQueueExecution() {
    UserWorkflowExecution userWorkflowExecutionRunning = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionRunning.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.create(userWorkflowExecutionRunning);
    UserWorkflowExecution runningOrInQueueExecution = userWorkflowExecutionDao
        .getRunningOrInQueueExecution(userWorkflowExecutionRunning.getDatasetName());
    Assert.assertEquals(WorkflowStatus.RUNNING, runningOrInQueueExecution.getWorkflowStatus());
  }

  @Test
  public void exists() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertTrue(userWorkflowExecutionDao.exists(userWorkflowExecution));
  }

  @Test
  public void cancel() {
    Assert.assertFalse(userWorkflowExecutionDao.cancel(null));
  }

  @Test
  public void existsAndNotCompleted() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertEquals(objectId, userWorkflowExecutionDao
        .existsAndNotCompleted(userWorkflowExecution.getDatasetName()));
  }

  @Test
  public void existsAndNotCompletedReturnNull() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertNull(
        userWorkflowExecutionDao.existsAndNotCompleted(userWorkflowExecution.getDatasetName()));
  }

  @Test
  public void getRunningUserWorkflowExecution() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    UserWorkflowExecution runningUserWorkflowExecution = userWorkflowExecutionDao
        .getRunningUserWorkflowExecution(userWorkflowExecution.getDatasetName(),
            userWorkflowExecution.getWorkflowOwner(), userWorkflowExecution.getWorkflowName());
    Assert.assertEquals(objectId, runningUserWorkflowExecution.getId().toString());
  }

  @Test
  public void getAllUserWorkflowExecutions() {
    int userWorkflowExecutionsToCreate =
        userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest() + 1;
    for (int i = 0; i < userWorkflowExecutionsToCreate; i++) {
      UserWorkflowExecution userWorkflowExecution = TestObjectFactory
          .createUserWorkflowExecutionObject();
      userWorkflowExecutionDao.create(userWorkflowExecution);
    }
    String nextPage = null;
    int allUserWorkflowsExecutionsCount = 0;
    do {
      ResponseListWrapper<UserWorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
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
      UserWorkflowExecution userWorkflowExecution = TestObjectFactory
          .createUserWorkflowExecutionObject();
      userWorkflowExecutionDao.create(userWorkflowExecution);
    }
    String nextPage = null;
    int allUserWorkflowsExecutionsCount = 0;
    do {
      ResponseListWrapper<UserWorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
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
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertTrue(userWorkflowExecutionDao.isCancelled(new ObjectId(objectId)));
  }

  @Test
  public void isCancelling() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setCancelling(true);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertTrue(userWorkflowExecutionDao.isCancelling(new ObjectId(objectId)));
  }

  @Test
  public void isExecutionActive() {
    Date beforeDate = new Date();
    Date afterDate = new Date(beforeDate.getTime() + 1000);
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setUpdatedDate(afterDate);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    UserWorkflowExecution retrievedUserWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    retrievedUserWorkflowExecution.setUpdatedDate(beforeDate);
    Assert
        .assertTrue(userWorkflowExecutionDao.isExecutionActive(retrievedUserWorkflowExecution, 0));
  }

  @Test
  public void isExecutionActiveInterrupted() throws InterruptedException {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    Thread t = new Thread(() -> Assert
        .assertTrue(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, 10)));
    t.start();
    Thread.sleep(100); // let the other thread start
    t.interrupt();
  }

  @Test
  public void isExecutionActiveFalse() {
    Date updatedDate = new Date();
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setUpdatedDate(updatedDate);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecution);
    UserWorkflowExecution retrievedUserWorkflowExecution = userWorkflowExecutionDao
        .getById(objectId);
    Assert
        .assertFalse(userWorkflowExecutionDao.isExecutionActive(retrievedUserWorkflowExecution, 0));
  }

  @Test
  public void removeActiveExecutionsFromList() {
    Date updatedDate = new Date();
    Date beforeDate = updatedDate;
    Date afterDate = new Date(beforeDate.getTime() + 1000);
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setUpdatedDate(updatedDate);
    userWorkflowExecutionDao.create(userWorkflowExecution);
    UserWorkflowExecution userWorkflowExecutionUpdated = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionUpdated.setUpdatedDate(afterDate);
    String objectId = userWorkflowExecutionDao.create(userWorkflowExecutionUpdated);
    userWorkflowExecutionUpdated.setUpdatedDate(beforeDate);

    List<UserWorkflowExecution> userWorkflowExecutions = new ArrayList<>();
    userWorkflowExecutions.add(userWorkflowExecution);
    userWorkflowExecutions.add(userWorkflowExecutionUpdated);
    Assert.assertEquals(2, userWorkflowExecutions.size());
    userWorkflowExecutionDao.removeActiveExecutionsFromList(userWorkflowExecutions, 0);
    Assert.assertEquals(1, userWorkflowExecutions.size());
  }

  @Test
  public void removeActiveExecutionsFromListInterrupted() throws InterruptedException {
    Thread t = new Thread(() -> {
      Date beforeDate = new Date();
      Date afterDate = new Date(beforeDate.getTime() + 1000);
      UserWorkflowExecution userWorkflowExecution = TestObjectFactory
          .createUserWorkflowExecutionObject();
      userWorkflowExecution.setUpdatedDate(new Date());
      userWorkflowExecution.setUpdatedDate(afterDate);
      userWorkflowExecutionDao.create(userWorkflowExecution);
      userWorkflowExecution.setUpdatedDate(beforeDate);
      List<UserWorkflowExecution> userWorkflowExecutions = new ArrayList<>();
      userWorkflowExecutions.add(userWorkflowExecution);
      userWorkflowExecutionDao.removeActiveExecutionsFromList(userWorkflowExecutions, 10);
      Assert.assertEquals(1, userWorkflowExecutions.size());
    });
    t.start();
    Thread.sleep(100); // let the other thread start
    t.interrupt();
  }

  @Test
  public void deleteAllByDatasetName() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(userWorkflowExecution);
    Assert.assertTrue(
        userWorkflowExecutionDao.deleteAllByDatasetName(userWorkflowExecution.getDatasetName()));
  }

  @Test
  public void updateAllDatasetNames() {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecutionDao.create(userWorkflowExecution);
    String updatedDatasetName = "updatedDatasetName";
    userWorkflowExecutionDao
        .updateAllDatasetNames(userWorkflowExecution.getDatasetName(), updatedDatasetName);
    userWorkflowExecution.setDatasetName(updatedDatasetName);
    Assert.assertTrue(userWorkflowExecutionDao.exists(userWorkflowExecution));
  }

}
