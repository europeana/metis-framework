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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ResultList;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.SystemId;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = new MorphiaDatastoreProviderImpl(mongoClient, "test");

    workflowExecutionDao = spy(new WorkflowExecutionDao(provider));
  }

  @BeforeEach
  void setup() {
    workflowExecutionDao.setWorkflowExecutionsPerRequest(5);
    workflowExecutionDao.setMaxServedExecutionListLength(10);
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.find(WorkflowExecution.class).delete(new DeleteOptions().multi(true));
    reset(workflowExecutionDao);
  }

  @Test
  void createUserWorkflowExecution() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
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
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
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
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    Date startedDate = new Date();
    workflowExecution.setStartedDate(startedDate);
    workflowExecution.setUpdatedDate(startedDate);
    workflowExecution.getMetisPlugins().get(0).setPluginStatus(PluginStatus.RUNNING);
    Date pluginUpdatedDate = new Date();
    if (workflowExecution.getMetisPlugins().get(0) instanceof AbstractExecutablePlugin) {
      workflowExecution.getMetisPlugins().get(0).setUpdatedDate(pluginUpdatedDate);
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
          updatedWorkflowExecution.getMetisPlugins().get(0).getUpdatedDate()));
    }
  }

  @Test
  void testSetCancellingState() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
    workflowExecutionDao.setCancellingState(workflowExecution, null);
    WorkflowExecution cancellingWorkflowExecution = workflowExecutionDao
        .getById(objectId);
    assertTrue(cancellingWorkflowExecution.isCancelling());
    assertEquals(SystemId.SYSTEM_MINUTE_CAP_EXPIRE.name(),
        cancellingWorkflowExecution.getCancelledBy());
  }

  @Test
  void getById() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    assertFalse(workflowExecution.isCancelling());
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
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
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
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

    final String executionFirstId = workflowExecutionDao.create(workflowExecutionFirst).getId().toString();
    final String executionSecondId = workflowExecutionDao.create(workflowExecutionSecond).getId().toString();

    PluginWithExecutionId<MetisPlugin> latestFinishedWorkflowExecution = workflowExecutionDao
        .getFirstOrLastFinishedPlugin(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    assertEquals(latestFinishedWorkflowExecution.getPlugin().getFinishedDate(),
        workflowExecutionSecond.getMetisPlugins().get(0).getFinishedDate());
    assertEquals(executionSecondId, latestFinishedWorkflowExecution.getExecutionId());

    PluginWithExecutionId<MetisPlugin> firstFinishedWorkflowExecution = workflowExecutionDao
        .getFirstOrLastFinishedPlugin(Integer.toString(TestObjectFactory.DATASETID),
            EnumSet.of(PluginType.OAIPMH_HARVEST), true);
    assertEquals(firstFinishedWorkflowExecution.getPlugin().getFinishedDate(),
        workflowExecutionFirst.getMetisPlugins().get(0).getFinishedDate());
    assertEquals(executionFirstId, firstFinishedWorkflowExecution.getExecutionId());
  }

  @Test
  void getFirstOrLastFinishedPlugin_isNull() {
    PluginWithExecutionId<MetisPlugin> latestFinishedWorkflowExecution = workflowExecutionDao
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

  private static class NonExecutableEnrichmentPlugin extends
      AbstractMetisPlugin<EnrichmentPluginMetadata> {

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
    doReturn(new PluginWithExecutionId<MetisPlugin>("", nonExecutableEnrichment))
        .when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.ENRICHMENT), false);
    doReturn(new PluginWithExecutionId<MetisPlugin>("", executableHarvest))
        .when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    doReturn(null).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.NORMALIZATION), false);

    // Check that the enrichment IS NOT returned by the method.
    assertNull(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.ENRICHMENT), false));

    // Check that the harvesting IS returned by the method.
    assertSame(executableHarvest,
        workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
            EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false).getPlugin());

    // Check that the normalization IS NOT returned by the method.
    assertNull(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.NORMALIZATION), false));
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
    doReturn(new PluginWithExecutionId<MetisPlugin>("", defaultPlugin)).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.OAIPMH_HARVEST), false);
    doReturn(new PluginWithExecutionId<MetisPlugin>("", validPlugin)).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.TRANSFORMATION), false);
    doReturn(new PluginWithExecutionId<MetisPlugin>("", deprecatedPlugin))
        .when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, EnumSet.of(PluginType.ENRICHMENT), false);

    // Try to find the default plugin
    assertSame(defaultPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), false).getPlugin());
    assertSame(defaultPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), true).getPlugin());

    // Try to find the valid plugin
    assertSame(validPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.TRANSFORMATION), false).getPlugin());
    assertSame(validPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.TRANSFORMATION), true).getPlugin());

    // Try to find the deprecated plugin
    assertSame(deprecatedPlugin, workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.ENRICHMENT), false).getPlugin());
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
    final PluginWithExecutionId<MetisPlugin> pluginWithExecutionId = new PluginWithExecutionId<>(
        "", plugin);
    doReturn(pluginWithExecutionId).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, pluginTypes, true);

    // Check the call
    assertSame(pluginWithExecutionId,
        workflowExecutionDao.getFirstSuccessfulPlugin(datasetId, pluginTypes));
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
    final PluginWithExecutionId<AbstractMetisPlugin> pluginWithExecutionId = new PluginWithExecutionId<>(
        "", plugin);
    doReturn(pluginWithExecutionId).when(workflowExecutionDao)
        .getFirstOrLastFinishedPlugin(datasetId, pluginTypes, false);

    // Check the call
    assertSame(pluginWithExecutionId,
        workflowExecutionDao.getLatestSuccessfulPlugin(datasetId, pluginTypes));
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
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
    WorkflowExecution runningWorkflowExecution = workflowExecutionDao
        .getById(workflowExecution.getId().toString());
    assertEquals(objectId, runningWorkflowExecution.getId().toString());
  }

  @Test
  void getAllUserWorkflowExecutions() {
    int userWorkflowExecutionsToCreate =
        workflowExecutionDao.getMaxServedExecutionListLength() + 1;
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
      final ResultList<WorkflowExecution> result = workflowExecutionDao.getAllWorkflowExecutions(
          Collections.singleton(Integer.toString(TestObjectFactory.DATASETID)), workflowStatuses,
          DaoFieldNames.ID, false, nextPage, 1, true);
      assertFalse(result.isMaxResultCountReached());
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(result.getResults(),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage,
          result.isMaxResultCountReached());
      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(userWorkflowExecutionsToCreate, allUserWorkflowsExecutionsCount);
  }

  @Test
  void getAllUserWorkflowExecutionsAscending() {
    int userWorkflowExecutionsToCreate =
        workflowExecutionDao.getMaxServedExecutionListLength() + 1;
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
      final ResultList<WorkflowExecution> result = workflowExecutionDao.getAllWorkflowExecutions(
              Collections.singleton(Integer.toString(TestObjectFactory.DATASETID)),
              workflowStatuses, DaoFieldNames.CREATED_DATE, true, nextPage, 1, false);
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(result.getResults(),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage,
          result.isMaxResultCountReached());
      if (!result.getResults().isEmpty()) {
        WorkflowExecution beforeWorkflowExecution =
            userWorkflowExecutionResponseListWrapper.getResults().get(0);
        for (int i = 1; i < userWorkflowExecutionResponseListWrapper.getListSize(); i++) {
          WorkflowExecution afterWorkflowExecution =
              userWorkflowExecutionResponseListWrapper.getResults().get(i);
          assertTrue(beforeWorkflowExecution.getCreatedDate()
              .before(afterWorkflowExecution.getCreatedDate()));
          beforeWorkflowExecution = afterWorkflowExecution;
        }
      }
      allUserWorkflowsExecutionsCount += userWorkflowExecutionResponseListWrapper.getListSize();
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();

      final boolean hasAll =
          allUserWorkflowsExecutionsCount == workflowExecutionDao.getMaxServedExecutionListLength();
      assertEquals(hasAll, result.isMaxResultCountReached());
    } while (nextPage != -1);

    assertEquals(workflowExecutionDao.getMaxServedExecutionListLength(),
        allUserWorkflowsExecutionsCount);
  }

  @Test
  void isCancelled() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
    assertTrue(workflowExecutionDao.isCancelled(new ObjectId(objectId)));
  }

  @Test
  void isCancelling() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setCancelling(true);
    String objectId = workflowExecutionDao.create(workflowExecution).getId().toString();
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
    final String finishedOldId = workflowExecutionDao.create(finishedOld).getId().toString();

    final WorkflowExecution cancelledOld = TestObjectFactory.createWorkflowExecutionObject();
    cancelledOld.setWorkflowStatus(WorkflowStatus.CANCELLED);
    cancelledOld.setCreatedDate(new Date(1));
    final Date startedDateOfCancelledPlugin = new Date(10);
    final List<AbstractMetisPlugin> metisPlugins = cancelledOld.getMetisPlugins();
    metisPlugins.forEach(metisPlugin -> {
      metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
      metisPlugin.setStartedDate(startedDateOfCancelledPlugin);
    });
    final String cancelledOldId = workflowExecutionDao.create(cancelledOld).getId().toString();

    final WorkflowExecution failedOld = TestObjectFactory.createWorkflowExecutionObject();
    failedOld.setWorkflowStatus(WorkflowStatus.FAILED);
    failedOld.setCreatedDate(new Date(0));
    final String failedOldId = workflowExecutionDao.create(failedOld).getId().toString();

    final WorkflowExecution finishedNew = TestObjectFactory.createWorkflowExecutionObject();
    finishedNew.setWorkflowStatus(WorkflowStatus.FINISHED);
    finishedNew.setCreatedDate(new Date(1000));
    final String finishedNewId = workflowExecutionDao.create(finishedNew).getId().toString();

    final WorkflowExecution runningOld = TestObjectFactory.createWorkflowExecutionObject();
    runningOld.setWorkflowStatus(WorkflowStatus.RUNNING);
    runningOld.setCreatedDate(new Date(0));
    final String runningOldId = workflowExecutionDao.create(runningOld).getId().toString();

    final WorkflowExecution runningNew = TestObjectFactory.createWorkflowExecutionObject();
    runningNew.setWorkflowStatus(WorkflowStatus.RUNNING);
    runningNew.setCreatedDate(new Date(1000));
    final String runningNewId = workflowExecutionDao.create(runningNew).getId().toString();

    final WorkflowExecution queuedOld = TestObjectFactory.createWorkflowExecutionObject();
    queuedOld.setWorkflowStatus(WorkflowStatus.INQUEUE);
    queuedOld.setCreatedDate(new Date(0));
    final String queuedOldId = workflowExecutionDao.create(queuedOld).getId().toString();

    final WorkflowExecution queuedNew = TestObjectFactory.createWorkflowExecutionObject();
    queuedNew.setWorkflowStatus(WorkflowStatus.INQUEUE);
    queuedNew.setCreatedDate(new Date(1000));
    final String queuedNewId = workflowExecutionDao.create(queuedNew).getId().toString();

    // Expected order
    final List<String> expectedOrder = Arrays
        .asList(queuedNewId, queuedOldId, runningNewId, runningOldId, finishedNewId, finishedOldId,
            cancelledOldId, failedOldId);

    // Try without filtering on dataset.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final ResultList<ExecutionDatasetPair> resultWithoutFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, 0, 1);
    assertNotNull(resultWithoutFilter);
    assertFalse(resultWithoutFilter.isMaxResultCountReached());
    final List<String> actualOrderWithoutFilter = resultWithoutFilter.getResults().stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
                                                                     .map(ObjectId::toString).toList();
    assertEquals(expectedOrder, actualOrderWithoutFilter);

    // Try with empty dataset ids Set.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final ResultList<ExecutionDatasetPair> resultWithEmptyDatasetIdsSet = workflowExecutionDao
        .getWorkflowExecutionsOverview(Collections.emptySet(), null, null, null, null, 0, 1);
    assertNotNull(resultWithEmptyDatasetIdsSet);
    assertFalse(resultWithEmptyDatasetIdsSet.isMaxResultCountReached());
    final List<String> actualOrderWithEmptyDatasetIdsSet = resultWithEmptyDatasetIdsSet.getResults().stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
                                                                                       .map(ObjectId::toString).toList();
    assertEquals(expectedOrder, actualOrderWithEmptyDatasetIdsSet);

    // Try with filtering on dataset.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final ResultList<ExecutionDatasetPair> resultWithFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(Collections.singleton("" + TestObjectFactory.DATASETID),
            null, null, null, null, 0,
            1);
    assertNotNull(resultWithFilter);
    assertFalse(resultWithFilter.isMaxResultCountReached());
    final List<String> actualOrderWithFilter = resultWithFilter.getResults().stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
                                                               .map(ObjectId::toString).toList();
    assertEquals(expectedOrder, actualOrderWithFilter);

    // Try with filtering on pluginStatuses and pluginTypes.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final ResultList<ExecutionDatasetPair> resultWithFilterPlugin = workflowExecutionDao
        .getWorkflowExecutionsOverview(null,
            EnumSet.of(PluginStatus.CANCELLED), EnumSet.of(PluginType.OAIPMH_HARVEST),
            startedDateOfCancelledPlugin, null, 0, 1);
    assertNotNull(resultWithFilterPlugin);
    assertFalse(resultWithFilterPlugin.isMaxResultCountReached());
    final List<String> actualOrderWithFilterPlugin = resultWithFilterPlugin.getResults().stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
                                                                           .map(ObjectId::toString).toList();
    assertEquals(Collections.singletonList(cancelledOldId), actualOrderWithFilterPlugin);
    assertEquals(2,
        resultWithFilterPlugin.getResults().get(0).getExecution().getMetisPlugins().size());

    // Try with filtering on pluginStatuses and pluginTypes that do not exist.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final ResultList<ExecutionDatasetPair> resultWithFilterPluginNoItems = workflowExecutionDao
        .getWorkflowExecutionsOverview(null,
            EnumSet.of(PluginStatus.FINISHED), EnumSet.of(PluginType.OAIPMH_HARVEST),
            null, null, 0, 1);
    assertNotNull(resultWithFilterPluginNoItems);
    assertFalse(resultWithFilterPluginNoItems.isMaxResultCountReached());
    final List<String> actualOrderWithFilterPluginNoItems = resultWithFilterPluginNoItems
        .getResults().stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
        .map(ObjectId::toString).toList();
    assertEquals(0, actualOrderWithFilterPluginNoItems.size());

    // Try with filter on non-existing dataset.
    workflowExecutionDao.setWorkflowExecutionsPerRequest(expectedOrder.size());
    final ResultList<ExecutionDatasetPair> resultWithInvalidFilter = workflowExecutionDao
        .getWorkflowExecutionsOverview(
            Collections.singleton("" + (TestObjectFactory.DATASETID + 1)), null, null, null, null,
            0, 1);
    assertNotNull(resultWithInvalidFilter);
    assertFalse(resultWithInvalidFilter.isMaxResultCountReached());
    assertTrue(resultWithInvalidFilter.getResults().isEmpty());

    // Try pagination
    final int pageSize = 2;
    final int pageNumber = 1;
    final int pageCount = 2;
    workflowExecutionDao.setWorkflowExecutionsPerRequest(pageSize);
    final ResultList<ExecutionDatasetPair> resultWithPaging = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, pageNumber, pageCount);
    assertNotNull(resultWithPaging);
    assertFalse(resultWithPaging.isMaxResultCountReached());
    final List<String> actualOrderWithPaging = resultWithPaging.getResults().stream()
        .map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
                                                               .map(ObjectId::toString).toList();
    assertEquals(expectedOrder.subList(pageSize * pageNumber, pageSize * (pageNumber + pageCount)),
        actualOrderWithPaging);

    // Test the max limit for results get last full page
    workflowExecutionDao.setMaxServedExecutionListLength(4);
    final ResultList<ExecutionDatasetPair> fullResultWithMaxServed = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, 1, 1);
    assertNotNull(fullResultWithMaxServed);
    assertTrue(fullResultWithMaxServed.isMaxResultCountReached());
    assertEquals(2, fullResultWithMaxServed.getResults().size());

    // Test the max limit for results get last partial page
    workflowExecutionDao.setMaxServedExecutionListLength(3);
    final ResultList<ExecutionDatasetPair> partialResultWithMaxServed = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, 1, 1);
    assertNotNull(partialResultWithMaxServed);
    assertTrue(partialResultWithMaxServed.isMaxResultCountReached());
    assertEquals(1, partialResultWithMaxServed.getResults().size());

    // Test the max limit for results get first empty page
    workflowExecutionDao.setMaxServedExecutionListLength(2);
    final ResultList<ExecutionDatasetPair> emptyResultWithMaxServed = workflowExecutionDao
        .getWorkflowExecutionsOverview(null, null, null, null, null, 1, 1);
    assertNotNull(emptyResultWithMaxServed);
    assertTrue(emptyResultWithMaxServed.isMaxResultCountReached());
    assertTrue(emptyResultWithMaxServed.getResults().isEmpty());
  }
}
