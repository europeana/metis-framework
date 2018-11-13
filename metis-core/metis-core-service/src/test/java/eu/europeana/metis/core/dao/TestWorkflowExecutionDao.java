package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-04
 */
class TestWorkflowExecutionDao {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeAll
  static void prepare() {
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

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(WorkflowExecution.class));
  }

  @Test
  void createUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution);
    assertNotNull(objectId);
  }

  @Test
  void updateUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionDao.create(workflowExecution);
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date updatedDate = new Date();
    workflowExecution.setUpdatedDate(updatedDate);
    String objectId = workflowExecutionDao.update(workflowExecution);
    assertNotNull(objectId);
    WorkflowExecution updatedWorkflowExecution = workflowExecutionDao.getById(objectId);
    assertEquals(WorkflowStatus.RUNNING, updatedWorkflowExecution.getWorkflowStatus());
    assertEquals(0, updatedDate.compareTo(updatedWorkflowExecution.getUpdatedDate()));
  }

  @Test
  void updateWorkflowPlugins() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    assertEquals(PluginStatus.INQUEUE,
        workflowExecution.getMetisPlugins().get(0).getPluginStatus());
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    WorkflowExecution updatedWorkflowExecution = workflowExecutionDao.getById(objectId);
    assertEquals(PluginStatus.RUNNING,
        updatedWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
  }

  @Test
  void updateMonitorInformation() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    Date createdDate = new Date();
    workflowExecution.setCreatedDate(createdDate);
    assertEquals(PluginStatus.INQUEUE,
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
    assertEquals(WorkflowStatus.RUNNING, updatedWorkflowExecution.getWorkflowStatus());
    assertEquals(0, createdDate.compareTo(updatedWorkflowExecution.getCreatedDate()));
    assertEquals(0, startedDate.compareTo(updatedWorkflowExecution.getStartedDate()));
    assertEquals(0, startedDate.compareTo(updatedWorkflowExecution.getUpdatedDate()));
    assertEquals(PluginStatus.RUNNING,
        updatedWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
    assertEquals(0, pluginUpdatedDate
        .compareTo(updatedWorkflowExecution.getMetisPlugins().get(0).getUpdatedDate()));
  }

  @Test
  void testSetCancellingState() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecutionDao.setCancellingState(workflowExecution);
    WorkflowExecution cancellingWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    assertTrue(cancellingWorkflowExecution.isCancelling());
  }

  @Test
  void getById() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    assertFalse(workflowExecution.isCancelling());
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution retrievedWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    assertEquals(workflowExecution.getCreatedDate(),
        retrievedWorkflowExecution.getCreatedDate());
    assertEquals(workflowExecution.getDatasetId(),
        retrievedWorkflowExecution.getDatasetId());
    assertEquals(workflowExecution.getWorkflowPriority(),
        retrievedWorkflowExecution.getWorkflowPriority());
    assertFalse(retrievedWorkflowExecution.isCancelling());
    assertEquals(workflowExecution.getMetisPlugins().get(0).getPluginType(),
        retrievedWorkflowExecution.getMetisPlugins().get(0).getPluginType());
  }

  @Test
  void delete() {
    assertFalse(workflowExecutionDao.delete(null));
  }

  @Test
  void getRunningOrInQueueExecution() {
    WorkflowExecution workflowExecutionRunning = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionRunning.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecutionDao.create(workflowExecutionRunning);
    WorkflowExecution runningOrInQueueExecution = workflowExecutionDao
        .getRunningOrInQueueExecution(workflowExecutionRunning.getDatasetId());
    assertEquals(WorkflowStatus.RUNNING, runningOrInQueueExecution.getWorkflowStatus());
  }

  @Test
  void exists() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionDao.create(workflowExecution);
    assertTrue(workflowExecutionDao.exists(workflowExecution));
  }

  @Test
  void existsAndNotCompleted() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = workflowExecutionDao.create(workflowExecution);
    assertEquals(objectId, workflowExecutionDao
        .existsAndNotCompleted(workflowExecution.getDatasetId()));
  }

  @Test
  void existsAndNotCompletedReturnNull() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionDao.create(workflowExecution);
    assertNull(
        workflowExecutionDao.existsAndNotCompleted(workflowExecution.getDatasetId()));
  }

  @Test
  void getLastFinishedWorkflowExecutionByDatasetIdAndPluginType() {

    WorkflowExecution workflowExecutionFirst = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionFirst.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionFirst.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));

    WorkflowExecution workflowExecutionSecond = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionSecond.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionSecond.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));
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
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    assertEquals(latestFinishedWorkflowExecutionByDatasetIdAndPluginType.getFinishedDate(),
        workflowExecutionSecond.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  void getLastFinishedWorkflowExecutionByDatasetIdAndPluginType_isNull() {
    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    assertNull(latestFinishedWorkflowExecutionByDatasetIdAndPluginType);
  }

  @Test
  void getFirstFinishedWorkflowExecutionByDatasetIdAndPluginType() {

    WorkflowExecution workflowExecutionFirst = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionFirst.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionFirst.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));

    WorkflowExecution workflowExecutionSecond = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionSecond.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionSecond.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));
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
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    assertEquals(firstFinishedWorkflowExecutionByDatasetIdAndPluginType.getFinishedDate(),
        workflowExecutionFirst.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  void getFirstFinishedWorkflowExecutionByDatasetIdAndPluginType_isNull() {
    AbstractMetisPlugin firstFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST));

    assertNull(firstFinishedWorkflowExecutionByDatasetIdAndPluginType);
  }

  @Test
  void getWorkflowExecutionByExecutionId() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    String objectId = workflowExecutionDao.create(workflowExecution);
    WorkflowExecution runningWorkflowExecution = workflowExecutionDao
        .getById(workflowExecution.getId().toString());
    assertEquals(objectId, runningWorkflowExecution.getId().toString());
  }

  @Test
  void getAllUserWorkflowExecutions() {
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
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(
          workflowExecutionDao.getAllWorkflowExecutions(
              Collections.singleton(Integer.toString(TestObjectFactory.DATASETID)),
              workflowStatuses, OrderField.ID, false, nextPage),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage);
      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

  @Test
  void getAllUserWorkflowExecutionsAscending() {
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
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(
          workflowExecutionDao.getAllWorkflowExecutions(
              Collections.singleton(Integer.toString(TestObjectFactory.DATASETID)),
              workflowStatuses, OrderField.CREATED_DATE, true, nextPage),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage);
      WorkflowExecution beforeWorkflowExecution = userWorkflowExecutionResponseListWrapper
          .getResults().get(0);
      for (int i = 1; i < userWorkflowExecutionResponseListWrapper.getListSize(); i++) {
        WorkflowExecution afterWorkflowExecution = userWorkflowExecutionResponseListWrapper
            .getResults().get(i);
        assertTrue(beforeWorkflowExecution.getCreatedDate()
            .before(afterWorkflowExecution.getCreatedDate()));
        beforeWorkflowExecution = afterWorkflowExecution;
      }

      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

  @Test
  void isCancelled() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
    String objectId = workflowExecutionDao.create(workflowExecution);
    assertTrue(workflowExecutionDao.isCancelled(new ObjectId(objectId)));
  }

  @Test
  void isCancelling() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setCancelling(true);
    String objectId = workflowExecutionDao.create(workflowExecution);
    assertTrue(workflowExecutionDao.isCancelling(new ObjectId(objectId)));
  }

  @Test
  void deleteAllByDatasetId() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionDao.create(workflowExecution);
    assertTrue(
        workflowExecutionDao.deleteAllByDatasetId(workflowExecution.getDatasetId()));
  }
}
