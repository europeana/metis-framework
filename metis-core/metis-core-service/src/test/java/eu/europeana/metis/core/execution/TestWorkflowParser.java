package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-01
 */
class TestWorkflowParser {

  private static final String DATASET_ID = Integer.toString(TestObjectFactory.DATASETID);
  private static WorkflowParser workflowParser;
  private static WorkflowExecutionDao workflowExecutionDao;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    workflowParser = spy(new WorkflowParser(workflowExecutionDao));
  }

  @AfterEach
  void cleanUp() {
    reset(workflowParser, workflowExecutionDao);
  }

  @Test
  void testGetPredecessorPlugin_HarvestPlugin() throws PluginExecutionNotAllowed {
    assertNull(
        workflowParser.getPredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST, null, DATASET_ID));
    assertNull(
        workflowParser.getPredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, null, DATASET_ID));
    assertNull(workflowParser.getPredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST,
        ExecutablePluginType.TRANSFORMATION, DATASET_ID));
    assertNull(workflowParser.getPredecessorPlugin(ExecutablePluginType.HTTP_HARVEST,
        ExecutablePluginType.TRANSFORMATION, DATASET_ID));
    Mockito.verify(workflowExecutionDao, Mockito.never())
        .getLatestSuccessfulPlugin(anyString(), any(), anyBoolean());
  }

  @Test
  void testGetPredecessorPlugin_EnforcedPluginType() throws PluginExecutionNotAllowed {

    // Create plugin object
    final AbstractExecutablePlugin plugin = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    doReturn(plugin).when(workflowExecutionDao).getLatestSuccessfulPlugin(DATASET_ID,
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST),true);

    // Test without errors
    plugin.setExecutionProgress(new ExecutionProgress());
    plugin.getExecutionProgress().setProcessedRecords(1);
    plugin.getExecutionProgress().setErrors(0);
    assertSame(plugin, workflowParser.getPredecessorPlugin(ExecutablePluginType.TRANSFORMATION,
        ExecutablePluginType.OAIPMH_HARVEST, DATASET_ID));

    // Test with errors
    plugin.getExecutionProgress().setErrors(1);
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.getPredecessorPlugin(
        ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.OAIPMH_HARVEST, DATASET_ID));
  }

  @Test
  void testGetPredecessorPlugin() throws PluginExecutionNotAllowed {
    final Set<ExecutablePluginType> pluginTypesSetThatPluginTypeCanBeBasedOn;
    pluginTypesSetThatPluginTypeCanBeBasedOn = new HashSet<>(
        WorkflowParser.getHarvestPluginGroup());
    testGetPredecessorPlugin(new ValidationExternalPluginMetadata(),
        pluginTypesSetThatPluginTypeCanBeBasedOn);
    testGetPredecessorPlugin(new TransformationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.VALIDATION_EXTERNAL));
    testGetPredecessorPlugin(new ValidationInternalPluginMetadata(),
        EnumSet.of(ExecutablePluginType.TRANSFORMATION));
    testGetPredecessorPlugin(new NormalizationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.VALIDATION_INTERNAL));
    testGetPredecessorPlugin(new EnrichmentPluginMetadata(),
        EnumSet.of(ExecutablePluginType.NORMALIZATION));
    testGetPredecessorPlugin(new MediaProcessPluginMetadata(),
        EnumSet.of(ExecutablePluginType.ENRICHMENT));
    testGetPredecessorPlugin(new IndexToPreviewPluginMetadata(),
        EnumSet.of(ExecutablePluginType.MEDIA_PROCESS));
    testGetPredecessorPlugin(new IndexToPublishPluginMetadata(),
        EnumSet.of(ExecutablePluginType.PREVIEW));
    testGetPredecessorPlugin(new LinkCheckingPluginMetadata(),
        WorkflowParser.getAllExceptLinkGroup());
    testGetPredecessorPlugin(new HTTPHarvestPluginMetadata(), Collections.emptySet());
    testGetPredecessorPlugin(new OaipmhHarvestPluginMetadata(), Collections.emptySet());
  }

  private void testGetPredecessorPlugin(ExecutablePluginMetadata metadata,
      Set<ExecutablePluginType> finishedPlugins) throws PluginExecutionNotAllowed {
    final AbstractExecutablePlugin plugin;
    if (!finishedPlugins.isEmpty()) {

      // Test without errors
      plugin = ExecutablePluginFactory.createPlugin(metadata);
      plugin.setExecutionProgress(new ExecutionProgress());
      plugin.getExecutionProgress().setProcessedRecords(1);
      plugin.getExecutionProgress().setErrors(0);
      when(workflowExecutionDao.getLatestSuccessfulPlugin(DATASET_ID, finishedPlugins, true))
          .thenReturn(plugin);
      assertSame(plugin, workflowParser
          .getPredecessorPlugin(metadata.getExecutablePluginType(), null, DATASET_ID));

      // Test with errors
      plugin.getExecutionProgress().setErrors(1);
      assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.getPredecessorPlugin(
          metadata.getExecutablePluginType(), null, DATASET_ID));
    } else {
      assertNull(workflowParser.getPredecessorPlugin(metadata.getExecutablePluginType(), null,
          DATASET_ID));
    }
  }

  @Test
  void testValidateWorkflowPlugins() throws GenericMetisException {
    
    // Create successful predecessor
    final ExecutablePluginType predecessorType = ExecutablePluginType.OAIPMH_HARVEST;
    final AbstractExecutablePlugin predecessor =
        ExecutablePluginFactory.createPlugin(new OaipmhHarvestPluginMetadata());
    predecessor.setExecutionProgress(new ExecutionProgress());
    predecessor.getExecutionProgress().setProcessedRecords(1);
    predecessor.getExecutionProgress().setErrors(0);
    doReturn(predecessor).when(workflowParser)
        .getPredecessorPlugin(any(), eq(predecessorType), eq(DATASET_ID));

    // Test allowed workflow
    assertSame(predecessor, workflowParser.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.OAIPMH_HARVEST), predecessorType));
    assertSame(predecessor, workflowParser.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.NORMALIZATION, ExecutablePluginType.ENRICHMENT, 
        ExecutablePluginType.LINK_CHECKING), predecessorType));
    assertSame(predecessor, workflowParser.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.ENRICHMENT, ExecutablePluginType.OAIPMH_HARVEST), predecessorType));

    // Test workflow with empty list
    assertThrows(BadContentException.class, () -> workflowParser
        .validateWorkflowPlugins(createWorkflow(), predecessorType));

    // Test workflow with null list
    final Workflow workflowWithNullList = new Workflow();
    workflowWithNullList.setMetisPluginsMetadata(null);
    assertThrows(BadContentException.class, () -> workflowParser
        .validateWorkflowPlugins(workflowWithNullList, predecessorType));

    // Test workflow with plugin with invalid type
    assertThrows(BadContentException.class, () -> workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.NORMALIZATION, null, 
            ExecutablePluginType.LINK_CHECKING), predecessorType));

    // Test workflow starting with link checking.
    assertSame(predecessor, workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.LINK_CHECKING), predecessorType));
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.LINK_CHECKING, ExecutablePluginType.TRANSFORMATION),
        predecessorType));

    // Test workflow with gaps
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.ENRICHMENT),
        predecessorType));

    // Test workflow with duplicate types
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.ENRICHMENT,
            ExecutablePluginType.ENRICHMENT), predecessorType));
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.LINK_CHECKING, 
            ExecutablePluginType.LINK_CHECKING), predecessorType));
    
    // Test workflow with disabled plugins: valid before disabling, but invalid after.
    final Workflow workflowWithDisabledPlugins = createWorkflow(ExecutablePluginType.NORMALIZATION,
        ExecutablePluginType.ENRICHMENT, ExecutablePluginType.MEDIA_PROCESS);
    assertSame(predecessor, workflowParser.validateWorkflowPlugins(workflowWithDisabledPlugins,
        predecessorType));
    doReturn(false).when(workflowWithDisabledPlugins.getMetisPluginsMetadata().get(1)).isEnabled();
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.validateWorkflowPlugins(
        workflowWithDisabledPlugins, predecessorType));
    
    // Test workflow with bad predecessor
    doThrow(PluginExecutionNotAllowed.class).when(workflowParser)
        .getPredecessorPlugin(any(), eq(predecessorType), eq(DATASET_ID));
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowParser.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.ENRICHMENT, ExecutablePluginType.OAIPMH_HARVEST),
        predecessorType));
  }

  private Workflow createWorkflow(ExecutablePluginType... pluginTypes) {
    final Workflow workflow = new Workflow();
    workflow.setDatasetId(DATASET_ID);
    workflow.setMetisPluginsMetadata(Arrays.stream(pluginTypes).map(type -> {
      final AbstractExecutablePluginMetadata plugin = mock(AbstractExecutablePluginMetadata.class);
      doReturn(true).when(plugin).isEnabled();
      doReturn(type).when(plugin).getExecutablePluginType();
      return plugin;
    }).collect(Collectors.toList()));
    return workflow;
  }
}
