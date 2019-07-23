package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.CancelledSystemId;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.ArrayList;
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
  void getFirstOrLastFinishedWorkflowExecutionByDatasetIdAndPluginType_CheckFirstAndLast() {

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

    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
            Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false, false);
    assertEquals(latestFinishedWorkflowExecutionByDatasetIdAndPluginType.getFinishedDate(),
        workflowExecutionSecond.getMetisPlugins().get(0).getFinishedDate());

    AbstractMetisPlugin firstFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
            Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false, true);
    assertEquals(firstFinishedWorkflowExecutionByDatasetIdAndPluginType.getFinishedDate(),
        workflowExecutionFirst.getMetisPlugins().get(0).getFinishedDate());
  }

  private static class NonExecutableEnrichmentPlugin extends AbstractMetisPlugin<EnrichmentPluginMetadata> {
    NonExecutableEnrichmentPlugin() {
      super(PluginType.ENRICHMENT);
    }
  }

  @Test
  void getFirstOrLastFinishedWorkflowExecutionByDatasetIdAndPluginType_CheckExecutable() {

    // Create workflow execution with non-executable version
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecution.setDatasetId(datasetId);
    final List<AbstractMetisPlugin> plugins = new ArrayList<>(workflowExecution.getMetisPlugins());
    plugins.add(new NonExecutableEnrichmentPlugin());
    workflowExecution.setMetisPlugins(plugins);
    plugins.forEach(plugin -> plugin.setFinishedDate(new Date()));
    plugins.forEach(plugin -> plugin.setPluginStatus(PluginStatus.FINISHED));
    plugins.forEach(plugin -> plugin.setId(new ObjectId().toString()));
    workflowExecutionDao.create(workflowExecution);

    // Check that the enrichment IS NOT returned by the method.
    assertNull(workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.ENRICHMENT), false, false));

    // Check that the harvesting IS returned by the method.
    assertEquals(plugins.get(0).getId(), workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(((AbstractExecutablePluginMetadata) plugins.get(0).getPluginMetadata())
                .getExecutablePluginType()), false, false).getId());
  }

  @Test
  void getFirstOrLastFinishedWorkflowExecutionByDatasetIdAndPluginType_CheckDataStatuses() {

    // Create workflow
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    workflowExecution.setDatasetId(datasetId);
    final List<AbstractMetisPlugin> plugins = new ArrayList<>();

    // Create harvesting plugin with default status
    final String defaultPluginId = new ObjectId().toString();
    final AbstractExecutablePlugin defaultPlugin = ExecutablePluginType.OAIPMH_HARVEST
        .getNewPlugin(new OaipmhHarvestPluginMetadata());
    defaultPlugin.setId(defaultPluginId);
    plugins.add(defaultPlugin);

    // Create transformation plugin with valid status
    final String validPluginId = new ObjectId().toString();
    final AbstractExecutablePlugin validPlugin = ExecutablePluginType.TRANSFORMATION
        .getNewPlugin(new TransformationPluginMetadata());
    validPlugin.setDataStatus(DataStatus.VALID);
    validPlugin.setId(validPluginId);
    plugins.add(validPlugin);

    // Create unreachable enrichment plugin with valid status
    final String unreachablePluginId = new ObjectId().toString();
    final AbstractExecutablePlugin unreachablePlugin = ExecutablePluginType.ENRICHMENT
        .getNewPlugin(new EnrichmentPluginMetadata());
    unreachablePlugin.setDataStatus(DataStatus.VALID);
    unreachablePlugin.setId(unreachablePluginId);
    plugins.add(unreachablePlugin);

    // Create enrichment plugin with deprecated status
    final String deprecatedPluginId = new ObjectId().toString();
    final AbstractExecutablePlugin deprecatedPlugin = ExecutablePluginType.ENRICHMENT
        .getNewPlugin(new EnrichmentPluginMetadata());
    deprecatedPlugin.setDataStatus(DataStatus.DEPRECATED);
    deprecatedPlugin.setId(deprecatedPluginId);
    plugins.add(deprecatedPlugin);

    // Add to the database
    plugins.forEach(plugin -> plugin.setFinishedDate(new Date()));
    unreachablePlugin.setFinishedDate(new Date(0));
    plugins.forEach(plugin -> plugin.setPluginStatus(PluginStatus.FINISHED));
    workflowExecution.setMetisPlugins(plugins);
    workflowExecutionDao.create(workflowExecution);

    // Try to find the default plugin
    assertEquals(defaultPluginId, workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false, true).getId());
    assertEquals(defaultPluginId, workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), true, true)
        .getId());

    // Try to find the valid plugin
    assertEquals(validPluginId, workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.TRANSFORMATION), false, true).getId());
    assertEquals(validPluginId, workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.TRANSFORMATION), true, true)
        .getId());

    // Try to find the deprecated plugin
    assertEquals(deprecatedPluginId, workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.ENRICHMENT), false, false).getId());
    assertNull(workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            EnumSet.of(ExecutablePluginType.ENRICHMENT), true, false));
  }

  @Test
  void getFirstOrLastFinishedWorkflowExecutionByDatasetIdAndPluginType_isNull() {
    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
            Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false, false);
    assertNull(latestFinishedWorkflowExecutionByDatasetIdAndPluginType);
  }

  @Test
  void getFirstFinishedWorkflowExecutionByDatasetIdAndPluginType() {

    // Set up the mock
    final AbstractExecutablePlugin plugin = ExecutablePluginType.MEDIA_PROCESS.getNewPlugin(null);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<ExecutablePluginType> pluginTypes = EnumSet
        .of(ExecutablePluginType.ENRICHMENT, ExecutablePluginType.MEDIA_PROCESS);
    doReturn(plugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            pluginTypes, false, true);

    // Check the call
    assertSame(plugin, workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, pluginTypes));
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            pluginTypes, false, true);
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(anyString(),
            any(), anyBoolean(), anyBoolean());
  }

  @Test
  void getLastFinishedWorkflowExecutionByDatasetIdAndPluginType() {

    // Set up the mock
    final AbstractExecutablePlugin plugin = ExecutablePluginType.MEDIA_PROCESS.getNewPlugin(null);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<ExecutablePluginType> pluginTypes = EnumSet
        .of(ExecutablePluginType.ENRICHMENT, ExecutablePluginType.MEDIA_PROCESS);
    doReturn(plugin).when(workflowExecutionDao)
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            pluginTypes, true, false);

    // Check the call
    assertSame(plugin, workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, pluginTypes,
            true));
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
            pluginTypes, true, false);
    verify(workflowExecutionDao, times(1))
        .getFirstOrLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(anyString(), any(),
            anyBoolean(), anyBoolean());
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
