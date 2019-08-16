package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
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
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
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
class TestWorkflowUtils {

  private static final String DATASET_ID = Integer.toString(TestObjectFactory.DATASETID);
  private static WorkflowUtils workflowUtils;
  private static WorkflowExecutionDao workflowExecutionDao;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    workflowUtils = spy(new WorkflowUtils(workflowExecutionDao));
  }

  @AfterEach
  void cleanUp() {
    reset(workflowUtils, workflowExecutionDao);
  }

  @Test
  void testGetPredecessorPlugin_HarvestPlugin() throws PluginExecutionNotAllowed {
    assertNull(
        workflowUtils.getPredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST, null, DATASET_ID));
    assertNull(
        workflowUtils.getPredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, null, DATASET_ID));
    assertNull(workflowUtils.getPredecessorPlugin(ExecutablePluginType.OAIPMH_HARVEST,
        ExecutablePluginType.TRANSFORMATION, DATASET_ID));
    assertNull(workflowUtils.getPredecessorPlugin(ExecutablePluginType.HTTP_HARVEST,
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
    assertSame(plugin, workflowUtils.getPredecessorPlugin(ExecutablePluginType.TRANSFORMATION,
        ExecutablePluginType.OAIPMH_HARVEST, DATASET_ID));

    // Test with errors
    plugin.getExecutionProgress().setErrors(1);
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.getPredecessorPlugin(
        ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.OAIPMH_HARVEST, DATASET_ID));
    
    // Test without progress information
    plugin.setExecutionProgress(null);
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.getPredecessorPlugin(
        ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.OAIPMH_HARVEST, DATASET_ID));
  }

  @Test
  void testGetPredecessorPlugin() throws PluginExecutionNotAllowed {
    final Set<ExecutablePluginType> pluginTypesSetThatPluginTypeCanBeBasedOn;
    pluginTypesSetThatPluginTypeCanBeBasedOn = new HashSet<>(
        WorkflowUtils.getHarvestPluginGroup());
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
        WorkflowUtils.getAllExceptLinkGroup());
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
      assertSame(plugin, workflowUtils
          .getPredecessorPlugin(metadata.getExecutablePluginType(), null, DATASET_ID));

      // Test with errors
      plugin.getExecutionProgress().setErrors(1);
      assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.getPredecessorPlugin(
          metadata.getExecutablePluginType(), null, DATASET_ID));
      
      // Test without progress information
      plugin.setExecutionProgress(null);
      assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.getPredecessorPlugin(
          metadata.getExecutablePluginType(), null, DATASET_ID));
    } else {
      assertNull(workflowUtils.getPredecessorPlugin(metadata.getExecutablePluginType(), null,
          DATASET_ID));
    }
  }

  @Test
  void testGetPredecessorPluginForWorkflowExecution() throws PluginExecutionNotAllowed {
    
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
        WorkflowUtils.getPredecessorPlugin(ExecutablePluginType.MEDIA_PROCESS, workflowExecution));

    // Execute the call for plugin type not requiring predecessor
    assertNull(
        WorkflowUtils.getPredecessorPlugin(ExecutablePluginType.HTTP_HARVEST, workflowExecution));

    // Execute the call for failed result
    assertThrows(IllegalArgumentException.class,
        () -> WorkflowUtils.getPredecessorPlugin(ExecutablePluginType.PUBLISH, workflowExecution));
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
    doReturn(predecessor).when(workflowUtils)
        .getPredecessorPlugin(any(), eq(predecessorType), eq(DATASET_ID));

    // Test allowed workflow
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.OAIPMH_HARVEST), predecessorType));
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.NORMALIZATION, ExecutablePluginType.ENRICHMENT, 
        ExecutablePluginType.LINK_CHECKING), predecessorType));
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(createWorkflow(
        ExecutablePluginType.ENRICHMENT, ExecutablePluginType.OAIPMH_HARVEST), predecessorType));

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

    // Test workflow starting with link checking.
    assertSame(predecessor, workflowUtils.validateWorkflowPlugins(
        createWorkflow(ExecutablePluginType.LINK_CHECKING), predecessorType));
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
        predecessorType));
    doReturn(false).when(workflowWithDisabledPlugins.getMetisPluginsMetadata().get(1)).isEnabled();
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
        workflowWithDisabledPlugins, predecessorType));
    
    // Test workflow with bad predecessor
    doThrow(PluginExecutionNotAllowed.class).when(workflowUtils)
        .getPredecessorPlugin(any(), eq(predecessorType), eq(DATASET_ID));
    assertThrows(PluginExecutionNotAllowed.class, () -> workflowUtils.validateWorkflowPlugins(
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
    workflow.setMetisPluginsMetadata(Arrays.asList(oai));
    workflowUtils.validateWorkflowPlugins(workflow, null);
    
    // Test output   
    assertEquals(simpleUrl, oai.getUrl());
    assertNull(oai.getMetadataFormat());
    assertNull(oai.getSetSpec());
    
    // Test OAI with invalid URL
    oai.setUrl("invalid URL");
    workflow.setMetisPluginsMetadata(Arrays.asList(oai));
    assertThrows(BadContentException.class,
        () -> workflowUtils.validateWorkflowPlugins(workflow, null));
   
    // Test HTTP with missing URL
    http.setUrl(null);
    workflow.setMetisPluginsMetadata(Arrays.asList(http));
    assertThrows(BadContentException.class,
        () -> workflowUtils.validateWorkflowPlugins(workflow, null));
  }
}
