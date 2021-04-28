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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.Pagination;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ResultList;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
class TestDataEvolutionUtils {

  private static final String DATASET_ID = Integer.toString(TestObjectFactory.DATASETID);
  private static DataEvolutionUtils dataEvolutionUtils;
  private static WorkflowExecutionDao workflowExecutionDao;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    dataEvolutionUtils = spy(new DataEvolutionUtils(workflowExecutionDao));
  }

  @AfterEach
  void cleanUp() {
    reset(dataEvolutionUtils, workflowExecutionDao);
  }

  @Test
  void testComputePredecessorPlugin_HarvestPlugin() throws PluginExecutionNotAllowed {
    assertNull(
        dataEvolutionUtils
            .computePredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST, null, DATASET_ID));
    assertNull(
        dataEvolutionUtils
            .computePredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, null, DATASET_ID));
    assertNull(dataEvolutionUtils.computePredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST,
        ExecutablePluginType.TRANSFORMATION, DATASET_ID));
    assertNull(dataEvolutionUtils.computePredecessorPlugin(ExecutablePluginType.HTTP_HARVEST,
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
        DataEvolutionUtils.getHarvestPluginGroup(), null);
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
        DataEvolutionUtils.getAllExceptLinkGroup(), null);

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
    boolean needsValidPredecessor =
            metadata.getExecutablePluginType() != ExecutablePluginType.DEPUBLISH;
    for (ExecutablePluginType predecessorType : predecessorTypes) {
      final AbstractExecutablePlugin predecessorPlugin = ExecutablePluginFactory
          .createPlugin(metadata);
      predecessorPlugin.setExecutionProgress(new ExecutionProgress());
      predecessorPlugin.getExecutionProgress().setProcessedRecords(1);
      predecessorPlugin.getExecutionProgress().setErrors(0);
      predecessorPlugin.setFinishedDate(new Date(counter));
      when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(DATASET_ID,
          Collections.singleton(predecessorType), needsValidPredecessor)).thenReturn(
          new PluginWithExecutionId<>(predecessorExecutionId.toString(), predecessorPlugin));
      recentPredecessorPlugin = predecessorPlugin;
      counter++;
    }
    if (predecessorTypes.isEmpty()
            || metadata.getExecutablePluginType() == ExecutablePluginType.DEPUBLISH) {
      assertNull(dataEvolutionUtils
          .computePredecessorPlugin(metadata.getExecutablePluginType(), enforcedPluginType,
              DATASET_ID));
    } else {
      assertNotNull(recentPredecessorPlugin);
      when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(DATASET_ID,
          DataEvolutionUtils.getHarvestPluginGroup(), true))
          .thenReturn(new PluginWithExecutionId<>(rootExecutionId.toString(), rootPlugin));
      when(workflowExecutionDao.getById(predecessorExecutionId.toString()))
          .thenReturn(predecessorExecution);
      final List<Pair<ExecutablePlugin, WorkflowExecution>> evolution =
          Arrays.asList(ImmutablePair.of(rootPlugin, rootExecution),
              ImmutablePair.of(mock(AbstractExecutablePlugin.class), rootExecution),
              ImmutablePair.of(mock(AbstractExecutablePlugin.class), rootExecution));
      when(dataEvolutionUtils.compileVersionEvolution(recentPredecessorPlugin, predecessorExecution))
          .thenReturn(evolution);

      // Test without errors
      final PluginWithExecutionId<ExecutablePlugin> withoutErrorsResult = dataEvolutionUtils
          .computePredecessorPlugin(metadata.getExecutablePluginType(), enforcedPluginType,
              DATASET_ID);
      assertSame(recentPredecessorPlugin, withoutErrorsResult.getPlugin());
      assertEquals(predecessorExecution.getId().toString(), withoutErrorsResult.getExecutionId());

      // Test when root plugin doesn't match
      final AbstractExecutablePlugin otherRootPlugin = mock(AbstractExecutablePlugin.class);
      final String otherRootPluginId = "other root plugin ID";
      when(otherRootPlugin.getId()).thenReturn(otherRootPluginId);
      when(dataEvolutionUtils.compileVersionEvolution(recentPredecessorPlugin, predecessorExecution))
          .thenReturn(
              Collections.singletonList(ImmutablePair.of(otherRootPlugin, rootExecution)));
      assertThrows(PluginExecutionNotAllowed.class,
          () -> dataEvolutionUtils.computePredecessorPlugin(metadata.getExecutablePluginType(),
              enforcedPluginType, DATASET_ID));
      when(dataEvolutionUtils.compileVersionEvolution(recentPredecessorPlugin, predecessorExecution))
          .thenReturn(Collections.singletonList(ImmutablePair.of(rootPlugin, rootExecution)));
      assertSame(recentPredecessorPlugin, dataEvolutionUtils.computePredecessorPlugin(
          metadata.getExecutablePluginType(), enforcedPluginType, DATASET_ID).getPlugin());

      // Test with errors
      recentPredecessorPlugin.getExecutionProgress().setErrors(1);
      assertThrows(PluginExecutionNotAllowed.class, () -> dataEvolutionUtils.computePredecessorPlugin(
          metadata.getExecutablePluginType(), enforcedPluginType, DATASET_ID));

      // Test without progress information
      recentPredecessorPlugin.setExecutionProgress(null);
      assertThrows(PluginExecutionNotAllowed.class, () -> dataEvolutionUtils.computePredecessorPlugin(
          metadata.getExecutablePluginType(), enforcedPluginType, DATASET_ID));
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
        DataEvolutionUtils
            .computePredecessorPlugin(ExecutablePluginType.MEDIA_PROCESS, workflowExecution));

    // Execute the call for plugin type not requiring predecessor
    assertNull(
        DataEvolutionUtils
            .computePredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, workflowExecution));

    // Execute the call for failed result
    assertThrows(IllegalArgumentException.class,
        () -> DataEvolutionUtils
            .computePredecessorPlugin(ExecutablePluginType.PUBLISH, workflowExecution));
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
    doReturn(null).when(dataEvolutionUtils).getPreviousExecutionAndPlugin(plugin1, datasetId);
    doReturn(new ImmutablePair<>(plugin1, execution1)).when(dataEvolutionUtils)
        .getPreviousExecutionAndPlugin(plugin2, datasetId);
    doReturn(new ImmutablePair<>(plugin2, execution2)).when(dataEvolutionUtils)
        .getPreviousExecutionAndPlugin(plugin3, datasetId);

    // Execute the call to examine all three
    final List<Pair<ExecutablePlugin, WorkflowExecution>> resultForThree = dataEvolutionUtils
        .compileVersionEvolution(plugin3, execution2);
    assertNotNull(resultForThree);
    assertEquals(2, resultForThree.size());
    assertSame(plugin1, resultForThree.get(0).getLeft());
    assertSame(execution1, resultForThree.get(0).getRight());
    assertSame(plugin2, resultForThree.get(1).getLeft());
    assertSame(execution2, resultForThree.get(1).getRight());

    // Execute the call to examine just two
    final List<Pair<ExecutablePlugin, WorkflowExecution>> resultForTwo = dataEvolutionUtils
        .compileVersionEvolution(plugin2, execution2);
    assertNotNull(resultForTwo);
    assertEquals(1, resultForTwo.size());
    assertSame(plugin1, resultForThree.get(0).getLeft());
    assertSame(execution1, resultForThree.get(0).getRight());

    // Execute the call to examine just one
    final List<Pair<ExecutablePlugin, WorkflowExecution>> resultForOne = dataEvolutionUtils
        .compileVersionEvolution(plugin1, execution1);
    assertNotNull(resultForOne);
    assertTrue(resultForOne.isEmpty());
  }

  private static WorkflowExecution createWorkflowExecution(String datasetId,
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
    assertNull(dataEvolutionUtils.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, null, null), datasetId));
    assertNull(dataEvolutionUtils.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, previousPluginType, null), datasetId));
    assertNull(dataEvolutionUtils.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, null, previousPluginTime), datasetId));

    // Test the absence of the execution despite the presence of the pointers.
    when(workflowExecutionDao
        .getByTaskExecution(eq(new ExecutedMetisPluginId(previousPluginTime, previousPluginType)), eq(datasetId)))
        .thenReturn(null);
    assertNull(dataEvolutionUtils.getPreviousExecutionAndPlugin(plugin, datasetId));
    when(workflowExecutionDao
        .getByTaskExecution(eq(new ExecutedMetisPluginId(previousPluginTime, previousPluginType)), eq(datasetId)))
        .thenReturn(previousExecution);

    // Test the absence of the plugin despite the presence of the pointers.
    when(previousExecution.getMetisPluginWithType(eq(previousPluginType))).thenReturn(
        Optional.empty());
    assertNull(dataEvolutionUtils.getPreviousExecutionAndPlugin(plugin, datasetId));
    when(previousExecution.getMetisPluginWithType(eq(previousPluginType)))
        .thenReturn(Optional.of(previousPlugin));

    // Test the happy flow
    final Pair<MetisPlugin, WorkflowExecution> result = dataEvolutionUtils
        .getPreviousExecutionAndPlugin(plugin, datasetId);
    assertNotNull(result);
    assertSame(previousExecution, result.getRight());
    assertSame(previousPlugin, result.getLeft());
  }

  private static AbstractMetisPlugin createMetisPlugin(PluginType type, PluginType previousType,
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

  @Test
  void testGetPublishedHarvestIncrements() {

    // Create a bunch of harvest and index plugins and link them
    final var fullOaiHarvest1 = createOaiHarvestPlugin(new Date(10), false, "A");
    final var incrementalOaiHarvest2 = createOaiHarvestPlugin(new Date(20), true, "B");
    final var httpHarvest3 = createExecutableMetisPlugin(ExecutablePluginType.HTTP_HARVEST,
            new Date(30), HTTPHarvestPlugin.class, HTTPHarvestPluginMetadata.class, "C");
    final var fullOaiHarvest4 = createOaiHarvestPlugin(new Date(40), false, "D");
    final var incrementalOaiHarvest5 = createOaiHarvestPlugin(new Date(50), true, "E");
    final var indexPlugin1 = createIndexToPublish(new Date(11), "F");
    final var indexPlugin2a = createIndexToPublish(new Date(21), "G");
    final var indexPlugin2b = createIndexToPublish(new Date(22), "H");
    final var indexPlugin3a = createIndexToPublish(new Date(31), "I");
    final var indexPlugin3b = createIndexToPublish(new Date(32), "J");
    final var indexPlugin4a = createIndexToPublish(new Date(41), "K");
    final var indexPlugin4b = createIndexToPublish(new Date(42), "L");
    final var indexPlugin5a = createIndexToPublish(new Date(51), "M");
    final var indexPlugin5b = createIndexToPublish(new Date(52), "N");
    doReturn(fullOaiHarvest1).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin1));
    doReturn(incrementalOaiHarvest2).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin2a));
    doReturn(incrementalOaiHarvest2).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin2b));
    doReturn(httpHarvest3).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin3a));
    doReturn(httpHarvest3).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin3b));
    doReturn(fullOaiHarvest4).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin4a));
    doReturn(fullOaiHarvest4).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin4b));
    doReturn(incrementalOaiHarvest5).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin5a));
    doReturn(incrementalOaiHarvest5).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin5b));

    // Test happy flow with two OAI harvests. Only last full and last incremented to be returned.
    final var listOfAllOaiIndex = List.of(indexPlugin5b, indexPlugin5a, indexPlugin4b,
            indexPlugin4a, indexPlugin2b, indexPlugin2a, indexPlugin1);
    doReturn(listOfAllOaiIndex).when(dataEvolutionUtils)
            .getPublishOperationsSortedInversely(DATASET_ID);
    final var result1 = dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID);
    assertListSameItems(List.of(fullOaiHarvest4, incrementalOaiHarvest5), result1);

    // Test happy flow with an http harvest
    final var listOfHttpAndOaiIndex = List.of(indexPlugin5b, indexPlugin5a, indexPlugin3b,
            indexPlugin3a, indexPlugin2b, indexPlugin2a, indexPlugin1);
    doReturn(listOfHttpAndOaiIndex).when(dataEvolutionUtils)
            .getPublishOperationsSortedInversely(DATASET_ID);
    final var result2 = dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID);
    assertListSameItems(List.of(httpHarvest3, incrementalOaiHarvest5), result2);

    // Test happy flow with just one full harvest
    doReturn(List.of(indexPlugin1)).when(dataEvolutionUtils)
            .getPublishOperationsSortedInversely(DATASET_ID);
    final var result3 = dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID);
    assertListSameItems(List.of(fullOaiHarvest1), result3);

    // Test flow with no harvest
    doReturn(Collections.emptyList()).when(dataEvolutionUtils)
            .getPublishOperationsSortedInversely(DATASET_ID);
    assertTrue(dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID).isEmpty());

    // Test flow with only an incremental harvest
    doReturn(List.of(indexPlugin5b)).when(dataEvolutionUtils)
            .getPublishOperationsSortedInversely(DATASET_ID);
    assertTrue(dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID).isEmpty());

    // Test flow with invalid harvests or non-harvests
    doReturn(List.of(indexPlugin5a, indexPlugin4a, indexPlugin1)).when(dataEvolutionUtils)
            .getPublishOperationsSortedInversely(DATASET_ID);
    doReturn(DataStatus.DELETED).when(indexPlugin4a.getPlugin()).getDataStatus();
    assertTrue(dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID).isEmpty());
    doReturn(DataStatus.DEPRECATED).when(indexPlugin4a.getPlugin()).getDataStatus();
    assertListSameItems(List.of(fullOaiHarvest4, incrementalOaiHarvest5),
            dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID));
    doReturn(DataStatus.VALID).when(indexPlugin4a.getPlugin()).getDataStatus();
    assertListSameItems(List.of(fullOaiHarvest4, incrementalOaiHarvest5),
            dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID));
    doReturn(indexPlugin4a).when(dataEvolutionUtils).getRootAncestor(same(indexPlugin4a));
    assertTrue(dataEvolutionUtils.getPublishedHarvestIncrements(DATASET_ID).isEmpty());
  }

  private <T extends MetisPlugin> void assertListSameItems(
          List<PluginWithExecutionId<? extends T>> expected,
          List<PluginWithExecutionId<T>> actual) {
    assertListSameItems(expected, actual, item -> item);
  }

  private <T, S> void assertListSameItems(List<T> expected, List<S> actual,
          Function<S, T> extractor) {
    assertNotNull(expected);
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertSame(expected.get(i), extractor.apply(actual.get(i)));
    }
  }

  private static <M extends AbstractExecutablePluginMetadata, T extends AbstractExecutablePlugin<M>>
  PluginWithExecutionId<T> createExecutableMetisPlugin(ExecutablePluginType type, Date startedDate,
          Class<T> pluginClass, Class<M> metadataClass, String executionId) {
    M metadata = mock(metadataClass);
    doReturn(type).when(metadata).getExecutablePluginType();
    T result = mock(pluginClass);
    when(result.getPluginType()).thenReturn(type.toPluginType());
    when(result.getPluginMetadata()).thenReturn(metadata);
    when(result.getStartedDate()).thenReturn(startedDate);
    return new PluginWithExecutionId<>(executionId, result);
  }

  private static PluginWithExecutionId<IndexToPublishPlugin> createIndexToPublish(Date startedDate,
          String executionId) {
    return createExecutableMetisPlugin(ExecutablePluginType.PUBLISH, startedDate,
            IndexToPublishPlugin.class, IndexToPublishPluginMetadata.class, executionId);
  }

  private static PluginWithExecutionId<OaipmhHarvestPlugin> createOaiHarvestPlugin(Date startedDate,
          boolean incremental, String executionId) {
    final PluginWithExecutionId<OaipmhHarvestPlugin> result = createExecutableMetisPlugin(
            ExecutablePluginType.OAIPMH_HARVEST, startedDate, OaipmhHarvestPlugin.class,
            OaipmhHarvestPluginMetadata.class, executionId);
    when(result.getPlugin().getPluginMetadata().isIncrementalHarvest()).thenReturn(incremental);
    return result;
  }

  @Test
  void testGetPublishOperationsSortedInversely(){

    // Create some objects
    final var otherPluginA = createOaiHarvestPlugin(new Date(0), false, null).getPlugin();
    final var indexPluginA = createIndexToPublish(new Date(1), null).getPlugin();
    final var indexPluginB1 = createIndexToPublish(new Date(2), null).getPlugin();
    final var indexPluginB2 = createIndexToPublish(new Date(3), null).getPlugin();
    final var executionA = createWorkflowExecution(DATASET_ID, otherPluginA, indexPluginA);
    final var executionB = createWorkflowExecution(DATASET_ID, indexPluginB1, indexPluginB2);
    final var pagination = mock(Pagination.class);

    // Test happy flow
    final var input = new ResultList<>(List.of(new ExecutionDatasetPair(new Dataset(), executionA),
            new ExecutionDatasetPair(new Dataset(), executionB)), true);
    doReturn(pagination).when(workflowExecutionDao).createPagination(0, null, true);
    doReturn(input).when(workflowExecutionDao)
            .getWorkflowExecutionsOverview(eq(Set.of(DATASET_ID)), eq(Set.of(PluginStatus.FINISHED)),
                    eq(Set.of(PluginType.PUBLISH)), isNull(), isNull(), same(pagination));
    final List<PluginWithExecutionId<IndexToPublishPlugin>> result1 = dataEvolutionUtils
            .getPublishOperationsSortedInversely(DATASET_ID);
    assertListSameItems(List.of(indexPluginB2, indexPluginB1, indexPluginA), result1,
            PluginWithExecutionId::getPlugin);

    // Test happy flow with different order
    doReturn(new Date(13)).when(indexPluginA).getStartedDate();
    doReturn(new Date(12)).when(indexPluginB1).getStartedDate();
    doReturn(new Date(11)).when(indexPluginB2).getStartedDate();
    final List<PluginWithExecutionId<IndexToPublishPlugin>> result2 = dataEvolutionUtils
            .getPublishOperationsSortedInversely(DATASET_ID);
    assertListSameItems(List.of(indexPluginA, indexPluginB1, indexPluginB2), result2,
            PluginWithExecutionId::getPlugin);

    // Test for no results
    doReturn(new ResultList<>(Collections.emptyList(), true)).when(workflowExecutionDao)
            .getWorkflowExecutionsOverview(eq(Set.of(DATASET_ID)), eq(Set.of(PluginStatus.FINISHED)),
                    eq(Set.of(PluginType.PUBLISH)), isNull(), isNull(), same(pagination));
    final List<PluginWithExecutionId<IndexToPublishPlugin>> result3 = dataEvolutionUtils
            .getPublishOperationsSortedInversely(DATASET_ID);
    assertListSameItems(Collections.emptyList(), result3, PluginWithExecutionId::getPlugin);
  }
}
