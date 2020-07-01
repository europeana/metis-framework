package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-01
 */
class TestWorkflowUtils {

  private static final String DATASET_ID = Integer.toString(TestObjectFactory.DATASETID);
  private static WorkflowUtils workflowUtils;
  private static WorkflowExecutionDao workflowExecutionDao;
  private static DepublishRecordIdDao depublishRecordIdDao;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    depublishRecordIdDao = mock(DepublishRecordIdDao.class);
    workflowUtils = spy(new WorkflowUtils(workflowExecutionDao, depublishRecordIdDao));
  }

  @AfterEach
  void cleanUp() {
    reset(workflowUtils, workflowExecutionDao, depublishRecordIdDao);
  }

  @Test
  void testComputePredecessorPlugin_HarvestPlugin() throws PluginExecutionNotAllowed {
    assertNull(
        workflowUtils
            .computePredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST, null, DATASET_ID));
    assertNull(
        workflowUtils
            .computePredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, null, DATASET_ID));
    assertNull(workflowUtils.computePredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST,
        ExecutablePluginType.TRANSFORMATION, DATASET_ID));
    assertNull(workflowUtils.computePredecessorPlugin(ExecutablePluginType.HTTP_HARVEST,
        ExecutablePluginType.TRANSFORMATION, DATASET_ID));
    Mockito.verify(workflowExecutionDao, Mockito.never())
        .getLatestSuccessfulExecutablePlugin(anyString(), any(), anyBoolean());
  }

  @Test
  void testComputePredecessorPlugin() throws PluginExecutionNotAllowed {

    // Test the actual predecessor types without enforcing a predecessor type.
    testComputePredecessorPlugin(new OaipmhHarvestPluginMetadata(), Collections.emptySet(), null);
    testComputePredecessorPlugin(new HTTPHarvestPluginMetadata(), Collections.emptySet(), null);
    testComputePredecessorPlugin(new ValidationExternalPluginMetadata(),
        WorkflowUtils.getHarvestPluginGroup(), null);
    testComputePredecessorPlugin(new TransformationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.VALIDATION_EXTERNAL), null);
    testComputePredecessorPlugin(new ValidationInternalPluginMetadata(),
        EnumSet.of(ExecutablePluginType.TRANSFORMATION), null);
    testComputePredecessorPlugin(new NormalizationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.VALIDATION_INTERNAL), null);
    testComputePredecessorPlugin(new EnrichmentPluginMetadata(),
        EnumSet.of(ExecutablePluginType.NORMALIZATION), null);
    testComputePredecessorPlugin(new MediaProcessPluginMetadata(),
        EnumSet.of(ExecutablePluginType.ENRICHMENT), null);
    testComputePredecessorPlugin(new IndexToPreviewPluginMetadata(),
        EnumSet.of(ExecutablePluginType.MEDIA_PROCESS), null);
    testComputePredecessorPlugin(new IndexToPublishPluginMetadata(),
        EnumSet.of(ExecutablePluginType.PREVIEW), null);
    testComputePredecessorPlugin(new DepublishPluginMetadata(),
        EnumSet.of(ExecutablePluginType.PUBLISH), null);
    testComputePredecessorPlugin(new LinkCheckingPluginMetadata(),
        WorkflowUtils.getAllExceptLinkGroup(), null);

    // Test enforcing a predecessor type.
    testComputePredecessorPlugin(new OaipmhHarvestPluginMetadata(), Collections.emptySet(),
        ExecutablePluginType.ENRICHMENT);
    testComputePredecessorPlugin(new HTTPHarvestPluginMetadata(), Collections.emptySet(),
        ExecutablePluginType.ENRICHMENT);
    testComputePredecessorPlugin(new TransformationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), ExecutablePluginType.OAIPMH_HARVEST);
  }

  private void testComputePredecessorPlugin(ExecutablePluginMetadata metadata,
      Set<ExecutablePluginType> predecessorTypes, ExecutablePluginType enforcedPluginType)
      throws PluginExecutionNotAllowed {
    if (!predecessorTypes.isEmpty()) {

      // Create some objects.
      final AbstractExecutablePlugin rootPlugin = mock(AbstractExecutablePlugin.class);
      final String rootPluginId = "root plugin ID";
      when(rootPlugin.getId()).thenReturn(rootPluginId);
      final WorkflowExecution rootExecution = new WorkflowExecution();
      final ObjectId rootExecutionId = new ObjectId(new Date(1));
      rootExecution.setId(rootExecutionId);
      final WorkflowExecution predecessorExecution = new WorkflowExecution();
      final ObjectId predecessorExecutionId = new ObjectId(new Date(2));
      predecessorExecution.setId(predecessorExecutionId);

      // Mock the DAO for the objects just created.
      int counter = 1;
      AbstractExecutablePlugin recentPredecessorPlugin = null;
      for (ExecutablePluginType predecessorType : predecessorTypes) {
        final AbstractExecutablePlugin predecessorPlugin = ExecutablePluginFactory
            .createPlugin(metadata);
        predecessorPlugin.setExecutionProgress(new ExecutionProgress());
        predecessorPlugin.getExecutionProgress().setProcessedRecords(1);
        predecessorPlugin.getExecutionProgress().setErrors(0);
        predecessorPlugin.setFinishedDate(new Date(counter));
        when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(DATASET_ID,
            Collections.singleton(predecessorType), true)).thenReturn(
            new PluginWithExecutionId<>(predecessorExecutionId.toString(), predecessorPlugin));
        recentPredecessorPlugin = predecessorPlugin;
        counter++;
      }
      assertNotNull(recentPredecessorPlugin);
      when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(DATASET_ID,
          WorkflowUtils.getHarvestPluginGroup(), true))
          .thenReturn(new PluginWithExecutionId<>(rootExecutionId.toString(), rootPlugin));
      when(workflowExecutionDao.getById(predecessorExecutionId.toString()))
          .thenReturn(predecessorExecution);
      final List<Pair<ExecutablePlugin, WorkflowExecution>> evolution =
          Arrays.asList(ImmutablePair.of(rootPlugin, rootExecution),
              ImmutablePair.of(mock(AbstractExecutablePlugin.class), rootExecution),
              ImmutablePair.of(mock(AbstractExecutablePlugin.class), rootExecution));
      when(workflowUtils.compileVersionEvolution(recentPredecessorPlugin, predecessorExecution))
          .thenReturn(evolution);

      // Test without errors
      final PluginWithExecutionId<ExecutablePlugin> withoutErrorsResult = workflowUtils
          .computePredecessorPlugin(metadata.getExecutablePluginType(), enforcedPluginType,
              DATASET_ID);
      assertSame(recentPredecessorPlugin, withoutErrorsResult.getPlugin());
      assertEquals(predecessorExecution.getId().toString(), withoutErrorsResult.getExecutionId());

      // Test when root plugin doesn't match
      final AbstractExecutablePlugin otherRootPlugin = mock(AbstractExecutablePlugin.class);
      final String otherRootPluginId = "other root plugin ID";
      when(otherRootPlugin.getId()).thenReturn(otherRootPluginId);
      when(workflowUtils.compileVersionEvolution(recentPredecessorPlugin, predecessorExecution))
          .thenReturn(Collections.singletonList(ImmutablePair.of(otherRootPlugin, rootExecution)));
      assertThrows(PluginExecutionNotAllowed.class,
          () -> workflowUtils.computePredecessorPlugin(metadata.getExecutablePluginType(),
              enforcedPluginType, DATASET_ID));
      when(workflowUtils.compileVersionEvolution(recentPredecessorPlugin, predecessorExecution))
          .thenReturn(Collections.singletonList(ImmutablePair.of(rootPlugin, rootExecution)));
      assertSame(recentPredecessorPlugin, workflowUtils.computePredecessorPlugin(
          metadata.getExecutablePluginType(), enforcedPluginType, DATASET_ID).getPlugin());

      // Test with errors
      recentPredecessorPlugin.getExecutionProgress().setErrors(1);
      assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.computePredecessorPlugin(
          metadata.getExecutablePluginType(), enforcedPluginType, DATASET_ID));

      // Test without progress information
      recentPredecessorPlugin.setExecutionProgress(null);
      assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.computePredecessorPlugin(
          metadata.getExecutablePluginType(), enforcedPluginType, DATASET_ID));
    } else {
      assertNull(workflowUtils
          .computePredecessorPlugin(metadata.getExecutablePluginType(), enforcedPluginType,
              DATASET_ID));
    }
  }

  @Test
  void testComputePredecessorPluginForWorkflowExecution() {

    // Add non executable plugin.
    final List<AbstractMetisPlugin> plugins = new ArrayList<>();
    plugins.add(new ReindexToPreviewPlugin(new ReindexToPreviewPluginMetadata()));

    // Add finished plugin of the wrong type.
    final AbstractMetisPlugin pluginOfWrongType =
        ExecutablePluginFactory.createPlugin(new TransformationPluginMetadata());
    pluginOfWrongType.setPluginStatus(PluginStatus.FINISHED);
    plugins.add(pluginOfWrongType);

    // Add two finished plugins of the right type.
    final AbstractMetisPlugin firstCandidate =
        ExecutablePluginFactory.createPlugin(new EnrichmentPluginMetadata());
    firstCandidate.setPluginStatus(PluginStatus.FINISHED);
    plugins.add(firstCandidate);
    final AbstractMetisPlugin lastCandidate =
        ExecutablePluginFactory.createPlugin(new EnrichmentPluginMetadata());
    lastCandidate.setPluginStatus(PluginStatus.FINISHED);
    plugins.add(lastCandidate);

    // Add non-finished plugin of the right type.
    final AbstractMetisPlugin pluginOfWrongStatus =
        ExecutablePluginFactory.createPlugin(new EnrichmentPluginMetadata());
    pluginOfWrongStatus.setPluginStatus(PluginStatus.CANCELLED);
    plugins.add(pluginOfWrongStatus);

    // Add all this to a workflow execution.
    final WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setMetisPlugins(plugins);

    // Execute the call expecting a successful result.
    assertSame(lastCandidate,
        WorkflowUtils
            .computePredecessorPlugin(ExecutablePluginType.MEDIA_PROCESS, workflowExecution));

    // Execute the call for plugin type not requiring predecessor
    assertNull(
        WorkflowUtils
            .computePredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, workflowExecution));

    // Execute the call for failed result
    assertThrows(IllegalArgumentException.class,
        () -> WorkflowUtils
            .computePredecessorPlugin(ExecutablePluginType.PUBLISH, workflowExecution));
  }

  @Test
  void testValidateWorkflowPlugins_testWorkflowComposition() throws GenericMetisException {

    // Create successful predecessor
    final ExecutablePluginType predecessorType = ExecutablePluginType.OAIPMH_HARVEST;
    final AbstractExecutablePlugin predecessor =
        ExecutablePluginFactory.createPlugin(new OaipmhHarvestPluginMetadata());
    predecessor.setExecutionProgress(new ExecutionProgress());
    predecessor.getExecutionProgress().setProcessedRecords(1);
    predecessor.getExecutionProgress().setErrors(0);
    doReturn(new PluginWithExecutionId<>("", predecessor)).when(workflowUtils)
        .computePredecessorPlugin(any(), eq(predecessorType), eq(DATASET_ID));

    // Test allowed workflow
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.OAIPMH_HARVEST), predecessorType).getPlugin());
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.NORMALIZATION, ExecutablePluginType.ENRICHMENT,
        ExecutablePluginType.LINK_CHECKING), predecessorType).getPlugin());
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.ENRICHMENT, ExecutablePluginType.OAIPMH_HARVEST), predecessorType)
        .getPlugin());

    // Test workflow with empty list
    assertThrows(BadContentException.class, () -> workflowUtils
        .validateWorkflowPlugins(createWorkflow(), predecessorType));

    // Test workflow with null list
    final Workflow workflowWithNullList = new Workflow();
    workflowWithNullList.setMetisPluginsMetadata(null);
    assertThrows(BadContentException.class, () -> workflowUtils
        .validateWorkflowPlugins(workflowWithNullList, predecessorType));

    // Test workflow with plugin with invalid type
    assertThrows(BadContentException.class, () -> workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.NORMALIZATION, null,
            ExecutablePluginType.LINK_CHECKING), predecessorType));

    // Test workflow with two plugins, one of which is depublish
    Workflow workflowDepublishAndOai = new Workflow();
    workflowDepublishAndOai.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setEnabled(true);
    DepublishPluginMetadata depublishPluginMetadata = new DepublishPluginMetadata();
    depublishPluginMetadata.setEnabled(true);
    depublishPluginMetadata.setDatasetDepublish(true);
    List<AbstractExecutablePluginMetadata> abstractMetisPluginMetadata = new ArrayList<>(2);
    abstractMetisPluginMetadata.add(oaipmhHarvestPluginMetadata);
    abstractMetisPluginMetadata.add(depublishPluginMetadata);
    workflowDepublishAndOai.setMetisPluginsMetadata(abstractMetisPluginMetadata);
    assertThrows(BadContentException.class,
        () -> workflowUtils.validateWorkflowPlugins(workflowDepublishAndOai, null));

    // Test if workflow contains record depublish that record ids exist
    Workflow workflowDepublish = new Workflow();
    workflowDepublish.setDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    depublishPluginMetadata.setDatasetDepublish(false);
    abstractMetisPluginMetadata.clear();
    abstractMetisPluginMetadata.add(depublishPluginMetadata);
    workflowDepublish.setMetisPluginsMetadata(abstractMetisPluginMetadata);
    when(depublishRecordIdDao
        .getAllDepublishRecordIdsWithStatus(workflowDepublish.getDatasetId(),
            DepublishRecordIdSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
            DepublicationStatus.PENDING_DEPUBLICATION)).thenReturn(Collections.emptySet());
    assertThrows(BadContentException.class, () -> workflowUtils
        .validateWorkflowPlugins(workflowDepublish, null));

    // Test workflow starting with link checking.
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.LINK_CHECKING), predecessorType).getPlugin());
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.LINK_CHECKING, ExecutablePluginType.TRANSFORMATION),
        predecessorType));

    // Test workflow with gaps
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.ENRICHMENT),
        predecessorType));

    // Test workflow with duplicate types
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.ENRICHMENT,
            ExecutablePluginType.ENRICHMENT), predecessorType));
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.LINK_CHECKING,
            ExecutablePluginType.LINK_CHECKING), predecessorType));

    // Test workflow with disabled plugins: valid before disabling, but invalid after.
    final Workflow workflowWithDisabledPlugins = createWorkflow(ExecutablePluginType.NORMALIZATION,
        ExecutablePluginType.ENRICHMENT, ExecutablePluginType.MEDIA_PROCESS);
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(workflowWithDisabledPlugins,
        predecessorType).getPlugin());
    when(workflowWithDisabledPlugins.getMetisPluginsMetadata().get(1).isEnabled())
        .thenReturn(false);
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        workflowWithDisabledPlugins, predecessorType));

    // Test workflow with bad predecessor
    doThrow(PluginExecutionNotAllowed.class).when(workflowUtils)
        .computePredecessorPlugin(any(), eq(predecessorType), eq(DATASET_ID));
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.ENRICHMENT, ExecutablePluginType.OAIPMH_HARVEST),
        predecessorType));
  }

  private Workflow createWorkflow(ExecutablePluginType... pluginTypes) {
    final Workflow workflow = new Workflow();
    workflow.setDatasetId(DATASET_ID);
    workflow.setMetisPluginsMetadata(Arrays.stream(pluginTypes).map(type -> {
      final AbstractExecutablePluginMetadata plugin = mock(AbstractExecutablePluginMetadata.class);
      when(plugin.isEnabled()).thenReturn(true);
      doReturn(type).when(plugin).getExecutablePluginType();
      return plugin;
    }).collect(Collectors.toList()));
    return workflow;
  }

  @Test
  void testValidateWorkflowPlugins_testHarvestingParameters() throws GenericMetisException {

    // Prepare correct url variables
    final String simpleUrl = "http://test.com/path";
    final String urlWithFragmentAndQuery = simpleUrl + "#fragment?query=1";
    final String metadataFormat = "metadataFormatParameter";
    final String setSpec = "setSpecParameter";

    // Create oai harvesting with all parameters
    final OaipmhHarvestPluginMetadata oai = new OaipmhHarvestPluginMetadata();
    oai.setEnabled(true);
    oai.setUrl(" " + urlWithFragmentAndQuery + " ");
    oai.setMetadataFormat(" " + metadataFormat + " ");
    oai.setSetSpec(" " + setSpec + " ");

    // Create http harvesting
    final HTTPHarvestPluginMetadata http = new HTTPHarvestPluginMetadata();
    http.setEnabled(true);
    http.setUrl(" " + urlWithFragmentAndQuery + " ");

    // Create the workflow and execute the method
    final Workflow workflow = new Workflow();
    workflow.setMetisPluginsMetadata(Arrays.asList(oai, http));
    workflowUtils.validateWorkflowPlugins(workflow, null);

    // Test output
    assertEquals(simpleUrl, oai.getUrl());
    assertEquals(metadataFormat, oai.getMetadataFormat());
    assertEquals(setSpec, oai.getSetSpec());
    assertEquals(urlWithFragmentAndQuery, http.getUrl());

    // Create oai harvesting with only url
    oai.setUrl(urlWithFragmentAndQuery);
    oai.setMetadataFormat(null);
    oai.setSetSpec(null);

    // Create the workflow and execute the method
    workflow.setMetisPluginsMetadata(Collections.singletonList(oai));
    workflowUtils.validateWorkflowPlugins(workflow, null);

    // Test output   
    assertEquals(simpleUrl, oai.getUrl());
    assertNull(oai.getMetadataFormat());
    assertNull(oai.getSetSpec());

    // Test OAI with invalid URL
    oai.setUrl("invalid URL");
    workflow.setMetisPluginsMetadata(Collections.singletonList(oai));
    assertThrows(BadContentException.class,
        () -> workflowUtils.validateWorkflowPlugins(workflow, null));

    // Test HTTP with missing URL
    http.setUrl(null);
    workflow.setMetisPluginsMetadata(Collections.singletonList(http));
    assertThrows(BadContentException.class,
        () -> workflowUtils.validateWorkflowPlugins(workflow, null));
  }

  @Test
  void testGetRecordEvolutionForVersionHappyFlow() {

    // Create two workflow executions with three plugins and link them together
    final String datasetId = "dataset ID";
    final AbstractExecutablePlugin plugin1 = mock(AbstractExecutablePlugin.class);
    final AbstractExecutablePlugin plugin2 = mock(AbstractExecutablePlugin.class);
    final AbstractExecutablePlugin plugin3 = mock(AbstractExecutablePlugin.class);
    final WorkflowExecution execution1 = createWorkflowExecution(datasetId, plugin1);
    final WorkflowExecution execution2 = createWorkflowExecution(datasetId, plugin2, plugin3);
    doReturn(null).when(workflowUtils).getPreviousExecutionAndPlugin(plugin1, datasetId);
    doReturn(new ImmutablePair<>(plugin1, execution1)).when(workflowUtils)
        .getPreviousExecutionAndPlugin(plugin2, datasetId);
    doReturn(new ImmutablePair<>(plugin2, execution2)).when(workflowUtils)
        .getPreviousExecutionAndPlugin(plugin3, datasetId);

    // Execute the call to examine all three
    final List<Pair<ExecutablePlugin, WorkflowExecution>> resultForThree = workflowUtils
        .compileVersionEvolution(plugin3, execution2);
    assertNotNull(resultForThree);
    assertEquals(2, resultForThree.size());
    assertSame(plugin1, resultForThree.get(0).getLeft());
    assertSame(execution1, resultForThree.get(0).getRight());
    assertSame(plugin2, resultForThree.get(1).getLeft());
    assertSame(execution2, resultForThree.get(1).getRight());

    // Execute the call to examine just two
    final List<Pair<ExecutablePlugin, WorkflowExecution>> resultForTwo = workflowUtils
        .compileVersionEvolution(plugin2, execution2);
    assertNotNull(resultForTwo);
    assertEquals(1, resultForTwo.size());
    assertSame(plugin1, resultForThree.get(0).getLeft());
    assertSame(execution1, resultForThree.get(0).getRight());

    // Execute the call to examine just one
    final List<Pair<ExecutablePlugin, WorkflowExecution>> resultForOne = workflowUtils
        .compileVersionEvolution(plugin1, execution1);
    assertNotNull(resultForOne);
    assertTrue(resultForOne.isEmpty());
  }

  private WorkflowExecution createWorkflowExecution(String datasetId,
      AbstractMetisPlugin... plugins) {
    final WorkflowExecution result = new WorkflowExecution();
    result.setId(new ObjectId());
    result.setDatasetId(datasetId);
    result.setMetisPlugins(Arrays.asList(plugins));
    return result;
  }

  @Test
  void testGetPreviousExecutionAndPlugin() {

    // Create some entities that we will be using.
    final String datasetId = "dataset id";
    final PluginType pluginType = PluginType.MEDIA_PROCESS;
    final PluginType previousPluginType = PluginType.OAIPMH_HARVEST;
    final Date previousPluginTime = new Date();
    final WorkflowExecution previousExecution = spy(new WorkflowExecution());
    final AbstractMetisPlugin previousPlugin = createMetisPlugin(previousPluginType, null, null);
    final AbstractMetisPlugin plugin = createMetisPlugin(pluginType, previousPluginType,
        previousPluginTime);

    // Test the absence of one or both of the pointers to a previous execution.
    assertNull(workflowUtils.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, null, null), datasetId));
    assertNull(workflowUtils.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, previousPluginType, null), datasetId));
    assertNull(workflowUtils.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, null, previousPluginTime), datasetId));

    // Test the absence of the execution despite the presence of the pointers.
    when(workflowExecutionDao
        .getByTaskExecution(eq(previousPluginTime), eq(previousPluginType), eq(datasetId)))
        .thenReturn(null);
    assertNull(workflowUtils.getPreviousExecutionAndPlugin(plugin, datasetId));
    when(workflowExecutionDao
        .getByTaskExecution(eq(previousPluginTime), eq(previousPluginType), eq(datasetId)))
        .thenReturn(previousExecution);

    // Test the absence of the plugin despite the presence of the pointers.
    when(previousExecution.getMetisPluginWithType(eq(previousPluginType))).thenReturn(
        Optional.empty());
    assertNull(workflowUtils.getPreviousExecutionAndPlugin(plugin, datasetId));
    when(previousExecution.getMetisPluginWithType(eq(previousPluginType)))
        .thenReturn(Optional.of(previousPlugin));

    // Test the happy flow
    final Pair<MetisPlugin, WorkflowExecution> result = workflowUtils
        .getPreviousExecutionAndPlugin(plugin, datasetId);
    assertNotNull(result);
    assertSame(previousExecution, result.getRight());
    assertSame(previousPlugin, result.getLeft());
  }

  private AbstractMetisPlugin createMetisPlugin(PluginType type, PluginType previousType,
      Date previousDate) {
    AbstractMetisPluginMetadata metadata = mock(AbstractMetisPluginMetadata.class);
    when(metadata.getPluginType()).thenReturn(type);
    when(metadata.getRevisionNamePreviousPlugin())
        .thenReturn(previousType == null ? null : previousType.name());
    when(metadata.getRevisionTimestampPreviousPlugin()).thenReturn(previousDate);
    AbstractMetisPlugin result = mock(AbstractMetisPlugin.class);
    when(result.getPluginType()).thenReturn(type);
    when(result.getPluginMetadata()).thenReturn(metadata);
    return result;
  }
}
