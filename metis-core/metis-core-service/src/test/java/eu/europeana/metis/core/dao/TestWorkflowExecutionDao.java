package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionIdAndStartedDatePair;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.CancelledSystemId;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
  private static MorphiaDatastoreProviderImpl provider;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    provider = new MorphiaDatastoreProviderImpl(mongoClient, "test");

    workflowExecutionDao = spy(new WorkflowExecutionDao(provider));
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
    reset(workflowExecutionDao);
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
    if (workflowExecution.getMetisPlugins().get(0) instanceof AbstractExecutablePlugin) {
      ((AbstractExecutablePlugin) workflowExecution.getMetisPlugins().get(0))
          .setUpdatedDate(pluginUpdatedDate);
    }
    workflowExecutionDao.updateMonitorInformation(workflowExecution);
    WorkflowExecution updatedWorkflowExecution = workflowExecutionDao.getById(objectId);
    assertEquals(WorkflowStatus.RUNNING, updatedWorkflowExecution.getWorkflowStatus());
    assertEquals(0, createdDate.compareTo(updatedWorkflowExecution.getCreatedDate()));
    assertEquals(0, startedDate.compareTo(updatedWorkflowExecution.getStartedDate()));
    assertEquals(0, startedDate.compareTo(updatedWorkflowExecution.getUpdatedDate()));
    assertEquals(PluginStatus.RUNNING,
        updatedWorkflowExecution.getMetisPlugins().get(0).getPluginStatus());
    if (workflowExecution.getMetisPlugins().get(0) instanceof AbstractExecutablePlugin) {
      assertEquals(0, pluginUpdatedDate.compareTo(
          ((AbstractExecutablePlugin) updatedWorkflowExecution.getMetisPlugins().get(0))
              .getUpdatedDate()));
    }
  }

  @Test
  void testSetCancellingState() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution);
    workflowExecutionDao.setCancellingState(workflowExecution, null);
    WorkflowExecution cancellingWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    assertTrue(cancellingWorkflowExecution.isCancelling());
    assertEquals(CancelledSystemId.SYSTEM_MINUTE_CAP_EXPIRE.name(),
        cancellingWorkflowExecution.getCancelledBy());
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
  void getFirstOrLastFinishedPlugin_CheckFirstAndLast() {

    WorkflowExecution workflowExecutionFirst = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecutionFirst.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionFirst.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));

    WorkflowExecution workflowExecutionSecond = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecutionSecond.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecutionSecond.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    for (int i = 0; i < workflowExecutionSecond.getMetisPlugins().size(); i++) {
      workflowExecutionFirst.getMetisPlugins().get(i).setFinishedDate(new Date());
      workflowExecutionSecond.getMetisPlugins().get(i).setFinishedDate(
          new Date(
              workflowExecutionFirst.getMetisPlugins().get(i).getFinishedDate().getTime() + 1000));
      workflowExecutionFirst.getMetisPlugins().get(i).setPluginStatus(PluginStatus.FINISHED);
      workflowExecutionSecond.getMetisPlugins().get(i).setPluginStatus(PluginStatus.FINISHED);
    }

    workflowExecutionDao.create(workflowExecutionFirst);
    workflowExecutionDao.create(workflowExecutionSecond);

    AbstractMetisPlugin latestFinishedWorkflowExecution = workflowExecutionDao
        .getFirstOrLastFinishedPlugin(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    assertEquals(latestFinishedWorkflowExecution.getFinishedDate(),
        workflowExecutionSecond.getMetisPlugins().get(0).getFinishedDate());

    AbstractMetisPlugin firstFinishedWorkflowExecution = workflowExecutionDao
        .getFirstOrLastFinishedPlugin(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST), true);
    assertEquals(firstFinishedWorkflowExecution.getFinishedDate(),
        workflowExecutionFirst.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  void getFirstOrLastFinishedPlugin_isNull() {
    AbstractMetisPlugin latestFinishedWorkflowExecution = workflowExecutionDao
        .getFirstOrLastFinishedPlugin(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    assertNull(latestFinishedWorkflowExecution);
  }

  @Test
  void getFirstOrLastFinishedPlugin_invalidPluginTypes() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    assertThrows(IllegalArgumentException.class,
        () -> workflowExecutionDao.getFirstOrLastFinishedPlugin(datasetId, null, true));
    assertThrows(IllegalArgumentException.class, () -> workflowExecutionDao
        .getFirstOrLastFinishedPlugin(datasetId, Collections.emptySet(), false));
    final Set<PluginType> setWithNull = new HashSet<>();
    setWithNull.add(null);
    setWithNull.add(PluginType.OAIPMH_HARVEST);
    assertThrows(IllegalArgumentException.class,
        () -> workflowExecutionDao.getFirstOrLastFinishedPlugin(datasetId, setWithNull, true));
  }

  private static class NonExecutableEnrichmentPlugin extends AbstractMetisPlugin<EnrichmentPluginMetadata> {
    NonExecutableEnrichmentPlugin() {
      super(PluginType.ENRICHMENT);
    }
  }

  @Test
  void getLatestSuccessfulExecutablePlugin_CheckExecutable() {

    // Create executable harvest and non-executable enrichment plugin
    final AbstractMetisPlugin nonExecutableEnrichment = new NonExecutableEnrichmentPlugin();
    final AbstractMetisPlugin executableHarvest = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());

    // Mock the dependent method
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doReturn(nonExecutableEnrichment).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.ENRICHMENT), false);
    doReturn(executableHarvest).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    doReturn(null).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.NORMALIZATION), false);

    // Check that the enrichment IS NOT returned by the method.
    assertNull(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.ENRICHMENT),false));

    // Check that the harvesting IS returned by the method.
    assertSame(executableHarvest, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false));

    // Check that the normalization IS NOT returned by the method.
    assertNull(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.NORMALIZATION),false));
  }

  @Test
  void getLatestSuccessfulExecutablePlugin_CheckDataStatuses() {

    // Create harvesting plugin with default status
    final AbstractExecutablePlugin defaultPlugin = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    defaultPlugin.setDataStatus(null);

    // Create transformation plugin with valid status
    final AbstractExecutablePlugin validPlugin = ExecutablePluginFactory
        .createPlugin(new TransformationPluginMetadata());
    validPlugin.setDataStatus(DataStatus.VALID);

    // Create unreachable enrichment plugin with valid status
    final AbstractExecutablePlugin unreachablePlugin = ExecutablePluginFactory
        .createPlugin(new EnrichmentPluginMetadata());
    unreachablePlugin.setDataStatus(DataStatus.VALID);

    // Create enrichment plugin with deprecated status
    final AbstractExecutablePlugin deprecatedPlugin = ExecutablePluginFactory
        .createPlugin(new EnrichmentPluginMetadata());
    deprecatedPlugin.setDataStatus(DataStatus.DEPRECATED);

    // Mock the dependent method
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doReturn(defaultPlugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    doReturn(validPlugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.TRANSFORMATION), false);
    doReturn(deprecatedPlugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.ENRICHMENT), false);

    // Try to find the default plugin
    assertSame(defaultPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false));
    assertSame(defaultPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), true));

    // Try to find the valid plugin
    assertSame(validPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.TRANSFORMATION), false));
    assertSame(validPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.TRANSFORMATION), true));

    // Try to find the deprecated plugin
    assertSame(deprecatedPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.ENRICHMENT), false));
    assertNull(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.ENRICHMENT), true));
  }

  @Test
  void getLatestSuccessfulExecutablePlugin_invalidPluginTypes() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    assertThrows(IllegalArgumentException.class,
        () -> workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId, null, true));
    assertThrows(IllegalArgumentException.class, () -> workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(datasetId, Collections.emptySet(), false));
    final Set<ExecutablePluginType> setWithNull = new HashSet<>();
    setWithNull.add(null);
    setWithNull.add(ExecutablePluginType.OAIPMH_HARVEST);
    assertThrows(IllegalArgumentException.class, () -> workflowExecutionDao
            .getLatestSuccessfulExecutablePlugin(datasetId, setWithNull, true));
  }

  @Test
  void getFirstSuccessfulPlugin() {

    // Set up the mock
    final AbstractExecutablePlugin plugin = ExecutablePluginFactory
        .createPlugin(new MediaProcessPluginMetadata());
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<PluginType> pluginTypes = EnumSet.of(PluginType.ENRICHMENT, PluginType.MEDIA_PROCESS);
    doReturn(plugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, pluginTypes, true);

    // Check the call
    assertSame(plugin, workflowExecutionDao.getFirstSuccessfulPlugin(datasetId, pluginTypes));
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedPlugin(datasetId, pluginTypes, true);
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedPlugin(anyString(), any(), anyBoolean());
  }

  @Test
  void getLatestSuccessfulPlugin() {

    // Set up the mock
    final AbstractExecutablePlugin plugin = ExecutablePluginFactory
        .createPlugin(new MediaProcessPluginMetadata());
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<PluginType> pluginTypes = EnumSet.of(PluginType.ENRICHMENT, PluginType.MEDIA_PROCESS);
    doReturn(plugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, pluginTypes, false);

    // Check the call
    assertSame(plugin, workflowExecutionDao.getLatestSuccessfulPlugin(datasetId, pluginTypes));
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedPlugin(datasetId, pluginTypes, false);
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedPlugin(anyString(), any(), anyBoolean());
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
              workflowStatuses, DaoFieldNames.ID, false, nextPage),
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
              workflowStatuses, DaoFieldNames.CREATED_DATE, true, nextPage),
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

  @Test
  void testGetAllExecutionStartDates() {

    // Try with empty list
    final String datasetId = "" + TestObjectFactory.DATASETID;
    final List<ExecutionIdAndStartedDatePair> emptyResult =  workflowExecutionDao
        .getAllExecutionStartDates(datasetId);
    assertNotNull(emptyResult);
    assertTrue(emptyResult.isEmpty());

    // Create three executions.
    final WorkflowExecution newestExecution = TestObjectFactory.createWorkflowExecutionObject();
    newestExecution.setStartedDate(new Date(1000));
    newestExecution.setDatasetId(datasetId);
    final String newestExecutionId = workflowExecutionDao.create(newestExecution);

    final WorkflowExecution oldestExecution = TestObjectFactory.createWorkflowExecutionObject();
    oldestExecution.setStartedDate(new Date(0));
    oldestExecution.setDatasetId(datasetId);
    final String oldestExecutionId = workflowExecutionDao.create(oldestExecution);

    final WorkflowExecution middleExecution = TestObjectFactory.createWorkflowExecutionObject();
    middleExecution.setStartedDate(new Date(500));
    middleExecution.setDatasetId(datasetId);
    final String middleExecutionId = workflowExecutionDao.create(middleExecution);

    // Outcome should not depend on pagination.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(1);

    // Make the call
    final List<ExecutionIdAndStartedDatePair> resultWithContent = workflowExecutionDao
        .getAllExecutionStartDates(datasetId);

    // Check
    assertNotNull(resultWithContent);
    assertEquals(3, resultWithContent.size());
    assertEquals(newestExecutionId, resultWithContent.get(0).getExecutionIdAsString());
    assertEquals(newestExecution.getStartedDate(), resultWithContent.get(0).getStartedDate());
    assertEquals(middleExecutionId, resultWithContent.get(1).getExecutionIdAsString());
    assertEquals(middleExecution.getStartedDate(), resultWithContent.get(1).getStartedDate());
    assertEquals(oldestExecutionId, resultWithContent.get(2).getExecutionIdAsString());
    assertEquals(oldestExecution.getStartedDate(), resultWithContent.get(2).getStartedDate());
  }

  @Test
  void getWorkflowExecutionOverview() {

    final WorkflowExecution finishedOld = TestObjectFactory.createWorkflowExecutionObject();
    finishedOld.setWorkflowStatus(WorkflowStatus.FINISHED);
    finishedOld.setCreatedDate(new Date(2));
    final String finishedOldId = workflowExecutionDao.create(finishedOld);

    final WorkflowExecution cancelledOld = TestObjectFactory.createWorkflowExecutionObject();
    cancelledOld.setWorkflowStatus(WorkflowStatus.CANCELLED);
    cancelledOld.setCreatedDate(new Date(1));
    final Date startedDateOfCancelledPlugin = new Date(10);
    final List<AbstractMetisPlugin> metisPlugins = cancelledOld.getMetisPlugins();
    metisPlugins.forEach(metisPlugin -> {
      metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
      metisPlugin.setStartedDate(startedDateOfCancelledPlugin);
    });
    final String cancelledOldId = workflowExecutionDao.create(cancelledOld);

    final WorkflowExecution failedOld = TestObjectFactory.createWorkflowExecutionObject();
    failedOld.setWorkflowStatus(WorkflowStatus.FAILED);
    failedOld.setCreatedDate(new Date(0));
    final String failedOldId = workflowExecutionDao.create(failedOld);

    final WorkflowExecution finishedNew = TestObjectFactory.createWorkflowExecutionObject();
    finishedNew.setWorkflowStatus(WorkflowStatus.FINISHED);
    finishedNew.setCreatedDate(new Date(1000));
    final String finishedNewId = workflowExecutionDao.create(finishedNew);

    final WorkflowExecution runningOld = TestObjectFactory.createWorkflowExecutionObject();
    runningOld.setWorkflowStatus(WorkflowStatus.RUNNING);
    runningOld.setCreatedDate(new Date(0));
    final String runningOldId = workflowExecutionDao.create(runningOld);

    final WorkflowExecution runningNew = TestObjectFactory.createWorkflowExecutionObject();
    runningNew.setWorkflowStatus(WorkflowStatus.RUNNING);
    runningNew.setCreatedDate(new Date(1000));
    final String runningNewId = workflowExecutionDao.create(runningNew);

    final WorkflowExecution queuedOld = TestObjectFactory.createWorkflowExecutionObject();
    queuedOld.setWorkflowStatus(WorkflowStatus.INQUEUE);
    queuedOld.setCreatedDate(new Date(0));
    final String queuedOldId = workflowExecutionDao.create(queuedOld);

    final WorkflowExecution queuedNew = TestObjectFactory.createWorkflowExecutionObject();
    queuedNew.setWorkflowStatus(WorkflowStatus.INQUEUE);
    queuedNew.setCreatedDate(new Date(1000));
    final String queuedNewId = workflowExecutionDao.create(queuedNew);

    // Expected order
    final List<String> expectedOrder = Arrays
        .asList(queuedNewId, queuedOldId, runningNewId, runningOldId, finishedNewId, finishedOldId,
            cancelledOldId, failedOldId);

    // Try without filtering on dataset.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final List<ExecutionDatasetPair> resultWithoutFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, 0, 1);
    assertNotNull(resultWithoutFilter);
    final List<String> actualOrderWithoutFilter = resultWithoutFilter.stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
        .map(ObjectId::toString).collect(Collectors.toList());
    assertEquals(expectedOrder, actualOrderWithoutFilter);

    // Try with filtering on dataset.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final List<ExecutionDatasetPair> resultWithFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(Collections.singleton("" + TestObjectFactory.DATASETID),
            null, null, null, null, 0,
            1);
    assertNotNull(resultWithFilter);
    final List<String> actualOrderWithFilter = resultWithFilter.stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
        .map(ObjectId::toString).collect(Collectors.toList());
    assertEquals(expectedOrder, actualOrderWithFilter);

    // Try with filtering on pluginStatuses and pluginTypes.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final List<ExecutionDatasetPair> resultWithFilterPlugin = workflowExecutionDao
        .getWorkflowExecutionsOverview(null,
            EnumSet.of(PluginStatus.CANCELLED), EnumSet.of(PluginType.OAIPMH_HARVEST),
            startedDateOfCancelledPlugin, null, 0, 1);
    assertNotNull(resultWithFilterPlugin);
    final List<String> actualOrderWithFilterPlugin = resultWithFilterPlugin.stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
        .map(ObjectId::toString).collect(Collectors.toList());
    assertEquals(Collections.singletonList(cancelledOldId), actualOrderWithFilterPlugin);
    assertEquals(2, resultWithFilterPlugin.get(0).getExecution().getMetisPlugins().size());

    // Try with filtering on pluginStatuses and pluginTypes that do not exist.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final List<ExecutionDatasetPair> resultWithFilterPluginNoItems = workflowExecutionDao
        .getWorkflowExecutionsOverview(null,
            EnumSet.of(PluginStatus.FINISHED), EnumSet.of(PluginType.OAIPMH_HARVEST),
            null, null, 0, 1);
    assertNotNull(resultWithFilterPluginNoItems);
    final List<String> actualOrderWithFilterPluginNoItems = resultWithFilterPluginNoItems.stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
        .map(ObjectId::toString).collect(Collectors.toList());
    assertEquals(0, actualOrderWithFilterPluginNoItems.size());

    // Try with filter on non-existing dataset.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final List<ExecutionDatasetPair> resultWithInvalidFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(
            Collections.singleton("" + (TestObjectFactory.DATASETID + 1)), null, null, null, null,
            0, 1);
    assertNotNull(resultWithInvalidFilter);
    assertTrue(resultWithInvalidFilter.isEmpty());

    // Try with empty filter.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final List<ExecutionDatasetPair> resultWithEmptyFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(Collections.emptySet(), null, null, null, null, 0, 1);
    assertNotNull(resultWithEmptyFilter);
    assertTrue(resultWithEmptyFilter.isEmpty());

    // Try pagination
    final int pageSize = 2;
    final int pageNumber = 1;
    final int pageCount = 2;
    workflowExecutionDao.setWorkflowExecutionsPerRequest(pageSize);
    final List<ExecutionDatasetPair> resultWithPaging = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, pageNumber, pageCount);
    assertNotNull(resultWithPaging);
    final List<String> actualOrderWithPaging = resultWithPaging.stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
        .map(ObjectId::toString).collect(Collectors.toList());
    assertEquals(expectedOrder.subList(pageSize * pageNumber, pageSize * (pageNumber + pageCount)),
        actualOrderWithPaging);
  }
}
