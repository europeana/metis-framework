package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-01
 */
class TestExecutionRules {

  private static WorkflowExecutionDao workflowExecutionDao;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(workflowExecutionDao);
  }

  @Test
  void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_HarvestPlugin() {
    assertNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        ExecutablePluginType.OAIPMH_HARVEST, null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
    assertNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        ExecutablePluginType.HTTP_HARVEST, null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
    Mockito.verify(workflowExecutionDao, Mockito.never())
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(anyString(), any(),
            anyBoolean());
  }

  @Test
  void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_EnforcedPluginType() {
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID),
        EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), true))
        .thenReturn(ExecutablePluginFactory.createPlugin(new OaipmhHarvestPluginMetadata()));
    assertNotNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.OAIPMH_HARVEST,
        Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }

  @Test
  void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution() {
    final Set<ExecutablePluginType> pluginTypesSetThatPluginTypeCanBeBasedOn;
    pluginTypesSetThatPluginTypeCanBeBasedOn = new HashSet<>(ExecutionRules.getHarvestPluginGroup());
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new ValidationExternalPluginMetadata(),
        pluginTypesSetThatPluginTypeCanBeBasedOn);
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new TransformationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.VALIDATION_EXTERNAL));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new ValidationInternalPluginMetadata(),
        EnumSet.of(ExecutablePluginType.TRANSFORMATION));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new NormalizationPluginMetadata(),
        EnumSet.of(ExecutablePluginType.VALIDATION_INTERNAL));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new EnrichmentPluginMetadata(),
        EnumSet.of(ExecutablePluginType.NORMALIZATION));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new MediaProcessPluginMetadata(),
        EnumSet.of(ExecutablePluginType.ENRICHMENT));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new IndexToPreviewPluginMetadata(),
        EnumSet.of(ExecutablePluginType.MEDIA_PROCESS));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new IndexToPublishPluginMetadata(),
        EnumSet.of(ExecutablePluginType.PREVIEW));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(new LinkCheckingPluginMetadata(),
        ExecutionRules.getAllExceptLinkGroup());
  }

  private void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      ExecutablePluginMetadata metadata, Set<ExecutablePluginType> finishedPlugins) {
    final AbstractExecutablePlugin plugin = ExecutablePluginFactory.createPlugin(metadata);
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID), finishedPlugins, true)).thenReturn(plugin);
    assertSame(plugin, ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        metadata.getExecutablePluginType(), null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
  }
}
