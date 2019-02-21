package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
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
        PluginType.OAIPMH_HARVEST, null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
    assertNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        PluginType.HTTP_HARVEST, null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
    Mockito.verify(workflowExecutionDao, Mockito.never())
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(Mockito.anyString(),
            Mockito.any());
  }

  @Test
  void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_EnforcedPluginType() {
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID), EnumSet.of(PluginType.OAIPMH_HARVEST)))
            .thenReturn(PluginType.OAIPMH_HARVEST.getNewPlugin(null));
    assertNotNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        PluginType.TRANSFORMATION, PluginType.OAIPMH_HARVEST,
        Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }

  @Test
  void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution() {
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.VALIDATION_EXTERNAL,
        ExecutionRules.getHarvestPluginGroup());
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.TRANSFORMATION,
        EnumSet.of(PluginType.VALIDATION_EXTERNAL));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.VALIDATION_INTERNAL,
        EnumSet.of(PluginType.TRANSFORMATION));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.NORMALIZATION,
        EnumSet.of(PluginType.VALIDATION_INTERNAL));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.ENRICHMENT,
        EnumSet.of(PluginType.NORMALIZATION));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.MEDIA_PROCESS,
        EnumSet.of(PluginType.ENRICHMENT));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.PREVIEW,
        EnumSet.of(PluginType.MEDIA_PROCESS));
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.PUBLISH,
        EnumSet.of(PluginType.PREVIEW));
    final Set<PluginType> allowedSetForLinkChecking = new HashSet<>(
        ExecutionRules.getProcessPluginGroup());
    allowedSetForLinkChecking.addAll(ExecutionRules.getIndexPluginGroup());
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.LINK_CHECKING,
        allowedSetForLinkChecking);
  }

  private void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      PluginType requestedPlugin, Set<PluginType> finishedPlugins) {
    final AbstractMetisPlugin plugin = requestedPlugin.getNewPlugin(null);
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID), finishedPlugins)).thenReturn(plugin);
    assertSame(plugin,
        ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(requestedPlugin,
            null, Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }
}
