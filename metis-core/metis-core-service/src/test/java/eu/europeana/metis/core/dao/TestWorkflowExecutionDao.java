package eu.europeana.metis.core.dao;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
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

  private static WorkflowExecutionDao workflowExecutionDao;
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

    workflowExecutionDao = new WorkflowExecutionDao(provider);
    workflowExecutionDao.setWorkflowExecutionsPerRequest(5);
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
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution);
    Assert.assertNotNull(objectId);
  }

  @Test
  public void updateUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionDao.create(workflowExecution);
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date updatedDate = new Date();
    workflowExecution.setUpdatedDate(updatedDate);
    String objectId = workflowExecutionDao.update(workflowExecution);
    Assert.assertNotNull(objectId);
    WorkflowExecution updatedWorkflowExecution = workflowExecutionDao.getById(objectId);
    Assert.assertEquals(WorkflowStatus.RUNNING, updatedWorkflowExecution.getWorkflowStatus());
    Assert.assertTrue(updatedDate.compareTo(updatedWorkflowExecution.getUpdatedDate()) == 0);
  }

  @Test
  public void updateWorkflowPlugins() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    Assert.assertEquals(PluginStatus.INQUEUE,
        workflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    WorkflowExecution updatedWorkflowExecution = workflowExecutionDao.getById(objectId);
    Assert.assertEquals(PluginStatus.RUNNING,
        updatedWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
  }

  @Test
  public void updateMonitorInformation() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    Date createdDate = new Date();
    workflowExecution.setCreatedDate(createdDate);
    Assert.assertEquals(PluginStatus.INQUEUE,
        workflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date startedDate = new Date();
    workflowExecution.setStartedDate(startedDate);
    workflowExecution.setUpdatedDate(startedDate);
    workflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    Date pluginUpdatedDate = new Date();
    workflowExecution.getMetisPlugins().get(0).setUpdatedDate(pluginUpdatedDate);
    workflowExecutionDao.updateMonitorInformation(workflowExecution);
    WorkflowExecution updatedWorkflowExecution = workflowExecutionDao.getById(objectId);
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
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecutionDao.setCancellingState(workflowExecution);
    WorkflowExecution cancellingWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    Assert.assertTrue(cancellingWorkflowExecution.isCancelling());
  }

  @Test
  public void getById() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    Assert.assertFalse(workflowExecution.isCancelling());
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    Assert.assertEquals(workflowExecution.getCreatedDate(),
        retrievedWorkflowExecution.getCreatedDate());
    Assert.assertEquals(workflowExecution.getDatasetId(),
        retrievedWorkflowExecution.getDatasetId());
    Assert.assertEquals(workflowExecution.getWorkflowOwner(),
        retrievedWorkflowExecution.getWorkflowOwner());
    Assert.assertEquals(workflowExecution.getWorkflowPriority(),
        retrievedWorkflowExecution.getWorkflowPriority());
    Assert.assertFalse(retrievedWorkflowExecution.isCancelling());
    Assert.assertEquals(workflowExecution.getMetisPlugins().get(0).getPluginType(),
        retrievedWorkflowExecution.getMetisPlugins().get(0).getPluginType());
  }

  @Test
  public void delete() {
    Assert.assertFalse(workflowExecutionDao.delete(null));
  }

  @Test
  public void getRunningOrInQueueExecution() {
    WorkflowExecution workflowExecutionRunning = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionRunning.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecutionDao.create(workflowExecutionRunning);
    WorkflowExecution runningOrInQueueExecution = workflowExecutionDao
        .getRunningOrInQueueExecution(workflowExecutionRunning.getDatasetId());
    Assert.assertEquals(WorkflowStatus.RUNNING, runningOrInQueueExecution.getWorkflowStatus());
  }

  @Test
  public void exists() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(workflowExecutionDao.exists(workflowExecution));
  }

  @Test
  public void existsAndNotCompleted() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = workflowExecutionDao.create(workflowExecution);
    Assert.assertEquals(objectId, workflowExecutionDao
        .existsAndNotCompleted(workflowExecution.getDatasetId()));
  }

  @Test
  public void existsAndNotCompletedReturnNull() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionDao.create(workflowExecution);
    Assert.assertNull(
        workflowExecutionDao.existsAndNotCompleted(workflowExecution.getDatasetId()));
  }

  @Test
  public void getLastFinishedWorkflowExecutionByDatasetIdAndPluginType() {

    WorkflowExecution workflowExecutionFirst = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionFirst.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionFirst.setDatasetId(TestObjectFactory.DATASETID);

    WorkflowExecution workflowExecutionSecond = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionSecond.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionSecond.setDatasetId(TestObjectFactory.DATASETID);
    for (int i = 0; i < workflowExecutionSecond.getMetisPlugins().size(); i++) {
      workflowExecutionFirst.getMetisPlugins().get(i).setFinishedDate(new Date());
      workflowExecutionSecond.getMetisPlugins().get(i).setFinishedDate(
          new Date(workflowExecutionFirst.getMetisPlugins().get(i).getFinishedDate().getTime() + 1000));
      workflowExecutionFirst.getMetisPlugins().get(i).setPluginStatus(PluginStatus.FINISHED);
      workflowExecutionSecond.getMetisPlugins().get(i).setPluginStatus(PluginStatus.FINISHED);
    }

    workflowExecutionDao.create(workflowExecutionFirst);
    workflowExecutionDao.create(workflowExecutionSecond);

    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    Assert.assertEquals(latestFinishedWorkflowExecutionByDatasetIdAndPluginType.getFinishedDate(),
        workflowExecutionSecond.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  public void getLastFinishedWorkflowExecutionByDatasetIdAndPluginType_isNull() {
    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    Assert.assertNull(latestFinishedWorkflowExecutionByDatasetIdAndPluginType);
  }

  @Test
  public void getFirstFinishedWorkflowExecutionByDatasetIdAndPluginType() {

    WorkflowExecution workflowExecutionFirst = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionFirst.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionFirst.setDatasetId(TestObjectFactory.DATASETID);

    WorkflowExecution workflowExecutionSecond = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionSecond.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionSecond.setDatasetId(TestObjectFactory.DATASETID);
    for (int i = 0; i < workflowExecutionSecond.getMetisPlugins().size(); i++) {
      workflowExecutionFirst.getMetisPlugins().get(i).setPluginStatus(PluginStatus.FINISHED);
      workflowExecutionSecond.getMetisPlugins().get(i).setPluginStatus(PluginStatus.FINISHED);
      workflowExecutionFirst.getMetisPlugins().get(i).setFinishedDate(new Date());
      workflowExecutionSecond.getMetisPlugins().get(i).setFinishedDate(
          new Date(workflowExecutionFirst.getMetisPlugins().get(i).getFinishedDate().getTime() + 1000));
    }

    workflowExecutionDao.create(workflowExecutionFirst);
    workflowExecutionDao.create(workflowExecutionSecond);

    AbstractMetisPlugin firstFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    Assert.assertEquals(firstFinishedWorkflowExecutionByDatasetIdAndPluginType.getFinishedDate(),
        workflowExecutionFirst.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  public void getFirstFinishedWorkflowExecutionByDatasetIdAndPluginType_isNull() {
    AbstractMetisPlugin firstFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    Assert.assertNull(firstFinishedWorkflowExecutionByDatasetIdAndPluginType);
  }

  @Test
  public void getWorkflowExecutionByExecutionId() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution runningWorkflowExecution = workflowExecutionDao
        .getById(workflowExecution.getId().toString());
    Assert.assertEquals(objectId, runningWorkflowExecution.getId().toString());
  }

  @Test
  public void getAllUserWorkflowExecutions() {
    int userWorkflowExecutionsToCreate =
        workflowExecutionDao.getWorkflowExecutionsPerRequest() + 1;
    for (int i = 0; i < userWorkflowExecutionsToCreate; i++) {
      WorkflowExecution workflowExecution = TestObjectFactory
          .createWorkflowExecutionObject();
      workflowExecutionDao.create(workflowExecution);
    }
    HashSet<WorkflowStatus> workflowStatuses = new HashSet<>();
    workflowStatuses.add(WorkflowStatus.INQUEUE);
    int nextPage = 0;
    int allUserWorkflowsExecutionsCount = 0;
    do {
      ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(workflowExecutionDao
              .getAllWorkflowExecutions(TestObjectFactory.DATASETID,
                  TestObjectFactory.WORKFLOWOWNER, workflowStatuses, OrderField.ID, false, nextPage),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage);
      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    Assert.assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

  @Test
  public void getAllUserWorkflowExecutionsAscending() {
    int userWorkflowExecutionsToCreate =
        workflowExecutionDao.getWorkflowExecutionsPerRequest() + 1;
    for (int i = 0; i < userWorkflowExecutionsToCreate; i++) {
      WorkflowExecution workflowExecution = TestObjectFactory
          .createWorkflowExecutionObject();
      workflowExecution.setCreatedDate(new Date(1000 * i));
      workflowExecutionDao.create(workflowExecution);
    }
    HashSet<WorkflowStatus> workflowStatuses = new HashSet<>();
    workflowStatuses.add(WorkflowStatus.INQUEUE);
    int nextPage = 0;
    int allUserWorkflowsExecutionsCount = 0;
    do {
      ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(workflowExecutionDao
              .getAllWorkflowExecutions(TestObjectFactory.DATASETID,
                  TestObjectFactory.WORKFLOWOWNER, workflowStatuses, OrderField.CREATED_DATE, true,
                  nextPage),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage);
      WorkflowExecution beforeWorkflowExecution = userWorkflowExecutionResponseListWrapper
          .getResults().get(0);
      for (int i = 1; i < userWorkflowExecutionResponseListWrapper.getListSize(); i++) {
        WorkflowExecution afterWorkflowExecution = userWorkflowExecutionResponseListWrapper
            .getResults().get(i);
        Assert.assertTrue(beforeWorkflowExecution.getCreatedDate()
            .before(afterWorkflowExecution.getCreatedDate()));
        beforeWorkflowExecution = afterWorkflowExecution;
      }

      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    Assert.assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

//  @Test
//  public void getAllUserWorkflowExecutionsByWorkflowStatus() {
//    int userWorkflowExecutionsToCreate =
//        workflowExecutionDao.getWorkflowExecutionsPerRequest() + 1;
//    for (int i = 0; i < userWorkflowExecutionsToCreate; i++) {
//      WorkflowExecution workflowExecution = TestObjectFactory
//          .createWorkflowExecutionObject();
//      workflowExecutionDao.create(workflowExecution);
//    }
//    int nextPage = 0;
//    int allUserWorkflowsExecutionsCount = 0;
//    do {
//      ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
//      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(workflowExecutionDao
//              .getAllWorkflowExecutions(TestObjectFactory.WORKFLOWOWNER, WorkflowStatus.INQUEUE, nextPage),
//          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage);
//      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
//      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
//    } while (nextPage != -1);
//
//    Assert.assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
//  }

  @Test
  public void isCancelled() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
    String objectId = workflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(workflowExecutionDao.isCancelled(new ObjectId(objectId)));
  }

  @Test
  public void isCancelling() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setCancelling(true);
    String objectId = workflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(workflowExecutionDao.isCancelling(new ObjectId(objectId)));
  }

  @Test
  public void isExecutionActiveUpdatedDateHasChanged() {
    Date beforeDate = new Date();
    Date afterDate = new Date(beforeDate.getTime() + 1000);
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(afterDate);
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    retrievedWorkflowExecution.setUpdatedDate(beforeDate);
    Assert
        .assertTrue(workflowExecutionDao.isExecutionActive(retrievedWorkflowExecution, 0));
  }

  @Test
  public void isExecutionActiveUpdatedDateGotValueFromNull() {
    Date updatedDate = new Date();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(updatedDate);
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    retrievedWorkflowExecution.setUpdatedDate(null);
    Assert
        .assertTrue(workflowExecutionDao.isExecutionActive(retrievedWorkflowExecution, 0));
  }

  @Test
  public void isExecutionActiveFinishedExecution() {
    Date updatedDate = new Date();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(updatedDate);
    workflowExecution.setFinishedDate(new Date());
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    Assert
        .assertTrue(workflowExecutionDao.isExecutionActive(retrievedWorkflowExecution, 0));
  }

  @Test
  public void isExecutionActiveInterrupted() throws InterruptedException {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    Thread t = new Thread(() -> Assert
        .assertTrue(workflowExecutionDao.isExecutionActive(workflowExecution, 10)));
    t.start();
    Awaitility.await().atMost(Duration.TWO_SECONDS).until(() -> untilThreadIsSleeping(t));
    t.interrupt();
  }

  @Test
  public void isExecutionActiveFalse() {
    Date updatedDate = new Date();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(updatedDate);
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    Assert
        .assertFalse(workflowExecutionDao.isExecutionActive(retrievedWorkflowExecution, 0));
  }

  @Test
  public void removeActiveExecutionsFromList() {
    Date updatedDate = new Date();
    Date beforeDate = updatedDate;
    Date afterDate = new Date(beforeDate.getTime() + 1000);
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setUpdatedDate(updatedDate);
    workflowExecutionDao.create(workflowExecution);
    WorkflowExecution workflowExecutionUpdated = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionUpdated.setUpdatedDate(afterDate);
    String objectId = workflowExecutionDao.create(workflowExecutionUpdated);
    workflowExecutionUpdated.setUpdatedDate(beforeDate);

    List<WorkflowExecution> workflowExecutions = new ArrayList<>();
    workflowExecutions.add(workflowExecution);
    workflowExecutions.add(workflowExecutionUpdated);
    Assert.assertEquals(2, workflowExecutions.size());
    workflowExecutionDao.removeActiveExecutionsFromList(workflowExecutions, 0);
    Assert.assertEquals(1, workflowExecutions.size());
  }

  @Test
  public void removeActiveExecutionsFromListInterrupted() throws InterruptedException {
    Thread t = new Thread(() -> {
      Date beforeDate = new Date();
      Date afterDate = new Date(beforeDate.getTime() + 1000);
      WorkflowExecution workflowExecution = TestObjectFactory
          .createWorkflowExecutionObject();
      workflowExecution.setUpdatedDate(new Date());
      workflowExecution.setUpdatedDate(afterDate);
      workflowExecutionDao.create(workflowExecution);
      workflowExecution.setUpdatedDate(beforeDate);
      List<WorkflowExecution> workflowExecutions = new ArrayList<>();
      workflowExecutions.add(workflowExecution);
      workflowExecutionDao.removeActiveExecutionsFromList(workflowExecutions, 10);
      Assert.assertEquals(1, workflowExecutions.size());
    });
    t.start();
    Awaitility.await().atMost(Duration.TWO_SECONDS).until(() -> untilThreadIsSleeping(t));
    t.interrupt();
  }

  @Test
  public void deleteAllByDatasetId() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionDao.create(workflowExecution);
    Assert.assertTrue(
        workflowExecutionDao.deleteAllByDatasetId(workflowExecution.getDatasetId()));
  }

  private void untilThreadIsSleeping(Thread t) {
    Assert.assertEquals("java.lang.Thread", t.getStackTrace()[0].getClassName());
    Assert.assertEquals("sleep", t.getStackTrace()[0].getMethodName());
  }

}
