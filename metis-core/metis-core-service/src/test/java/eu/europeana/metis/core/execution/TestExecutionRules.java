package eu.europeana.metis.core.execution;

import static org.mockito.Mockito.when;
import java.util.EnumSet;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-01
 */
public class TestExecutionRules {

  private static WorkflowExecutionDao workflowExecutionDao;

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_HarvestPlugin() {
    Assert.assertNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        PluginType.OAIPMH_HARVEST, null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
    Assert.assertNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        PluginType.HTTP_HARVEST, null, Integer.toString(TestObjectFactory.DATASETID),
        workflowExecutionDao));
    Mockito.verify(workflowExecutionDao, Mockito.never())
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(Mockito.anyString(),
            Mockito.any());
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_EnforcedPluginType() {
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID), EnumSet.of(PluginType.OAIPMH_HARVEST)))
            .thenReturn(PluginType.OAIPMH_HARVEST.getNewPlugin(null));
    Assert.assertNotNull(ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
        PluginType.TRANSFORMATION, PluginType.OAIPMH_HARVEST,
        Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }

  @Test
  public void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution() {
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
    testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.LINK_CHECKING,
        EnumSet.of(PluginType.VALIDATION_INTERNAL, PluginType.NORMALIZATION, PluginType.ENRICHMENT,
            PluginType.MEDIA_PROCESS, PluginType.PREVIEW, PluginType.PUBLISH));
  }

  private void testGetLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      PluginType requestedPlugin, Set<PluginType> finishedPlugins) {
    final AbstractMetisPlugin plugin = requestedPlugin.getNewPlugin(null);
    when(workflowExecutionDao.getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
        Integer.toString(TestObjectFactory.DATASETID), finishedPlugins)).thenReturn(plugin);
    Assert.assertSame(plugin,
        ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(requestedPlugin,
            null, Integer.toString(TestObjectFactory.DATASETID), workflowExecutionDao));
  }
}
