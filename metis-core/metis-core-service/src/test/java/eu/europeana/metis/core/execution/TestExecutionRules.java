package eu.europeana.metis.core.execution;

import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPlugin;
import java.util.EnumSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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
    Assert.assertNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.OAIPMH_HARVEST,
            null, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_ValidationExternalPlugin() {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            ExecutionRules.getHarvestPluginGroup())).thenReturn(new OaipmhHarvestPlugin());
    Assert.assertNotNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.VALIDATION_EXTERNAL,
            null, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_TransformationPlugin() {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.VALIDATION_EXTERNAL))).thenReturn(new ValidationExternalPlugin());
    Assert.assertNotNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.TRANSFORMATION,
            null, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_TransformationPlugin_EnforcedPluginType() {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.OAIPMH_HARVEST))).thenReturn(new OaipmhHarvestPlugin());
    Assert.assertNotNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.TRANSFORMATION,
            PluginType.OAIPMH_HARVEST, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_ValidationInternalPlugin() {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.TRANSFORMATION))).thenReturn(new ValidationExternalPlugin());
    Assert.assertNotNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.VALIDATION_INTERNAL,
            null, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_EnrichmentPlugin() {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet.of(PluginType.VALIDATION_INTERNAL))).thenReturn(new EnrichmentPlugin());
    Assert.assertNotNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.ENRICHMENT,
            null, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

  @Test
  public void getLatestFinishedPluginIfRequestedPluginAllowedForExecution_IndexPlugin() {
    Assert.assertNull(ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(PluginType.PREVIEW,
            null, TestObjectFactory.DATASETID, workflowExecutionDao));
  }

}
