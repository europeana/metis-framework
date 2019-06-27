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
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
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
        .thenReturn(ExecutablePluginType.OAIPMH_HARVEST.getNewPlugin(null));
    assertNotNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        ExecutablePluginType.TRANSFORMATION, ExecutablePluginType.OAIPMH_HARVEST,
        Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }

  @Test
  void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution() {
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.VALIDATION_EXTERNAL,
        ExecutionRules.getHarvestPluginGroup());
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.TRANSFORMATION,
        EnumSet.of(ExecutablePluginType.VALIDATION_EXTERNAL));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.VALIDATION_INTERNAL,
        EnumSet.of(ExecutablePluginType.TRANSFORMATION));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.NORMALIZATION,
        EnumSet.of(ExecutablePluginType.VALIDATION_INTERNAL));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.ENRICHMENT,
        EnumSet.of(ExecutablePluginType.NORMALIZATION));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.MEDIA_PROCESS,
        EnumSet.of(ExecutablePluginType.ENRICHMENT));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.PREVIEW,
        EnumSet.of(ExecutablePluginType.MEDIA_PROCESS));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.PUBLISH,
        EnumSet.of(ExecutablePluginType.PREVIEW));
    final Set<ExecutablePluginType> allowedSetForLinkChecking = new HashSet<>(
        ExecutionRules.getProcessPluginGroup());
    allowedSetForLinkChecking.addAll(EnumSet.allOf(ExecutablePluginType.class));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(ExecutablePluginType.LINK_CHECKING,
        allowedSetForLinkChecking);
  }

  private void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      ExecutablePluginType requestedPlugin, Set<ExecutablePluginType> finishedPlugins) {
    final AbstractExecutablePlugin plugin = requestedPlugin.getNewPlugin(null);
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID), finishedPlugins, true)).thenReturn(plugin);
    assertSame(plugin,
        ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(requestedPlugin,
            null, Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }
}
