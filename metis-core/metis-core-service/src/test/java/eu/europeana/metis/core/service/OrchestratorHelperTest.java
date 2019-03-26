package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrchestratorHelperTest {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static DatasetXsltDao datasetXsltDao;
  private static OrchestratorHelper orchestratorHelper;

  @BeforeAll
  static void setupMocks() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    datasetXsltDao = mock(DatasetXsltDao.class);
    orchestratorHelper = spy(new OrchestratorHelper(workflowExecutionDao, datasetXsltDao));
  }

  @BeforeEach
  void resetMocks() {
    reset(workflowExecutionDao, datasetXsltDao, orchestratorHelper);
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
    assertNull(orchestratorHelper.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, null, null), datasetId));
    assertNull(orchestratorHelper.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, previousPluginType, null), datasetId));
    assertNull(orchestratorHelper.getPreviousExecutionAndPlugin(createMetisPlugin(
        pluginType, null, previousPluginTime), datasetId));

    // Test the absence of the execution despite the presence of the pointers.
    when(workflowExecutionDao
        .getByTaskExecution(eq(previousPluginTime), eq(previousPluginType), eq(datasetId)))
        .thenReturn(null);
    assertNull(orchestratorHelper.getPreviousExecutionAndPlugin(plugin, datasetId));
    when(workflowExecutionDao
        .getByTaskExecution(eq(previousPluginTime), eq(previousPluginType), eq(datasetId)))
        .thenReturn(previousExecution);

    // Test the absence of the plugin despite the presence of the pointers.
    when(previousExecution.getMetisPluginWithType(eq(previousPluginType))).thenReturn(
        Optional.empty());
    assertNull(orchestratorHelper.getPreviousExecutionAndPlugin(plugin, datasetId));
    when(previousExecution.getMetisPluginWithType(eq(previousPluginType)))
        .thenReturn(Optional.of(previousPlugin));

    // Test the happy flow
    final Pair<WorkflowExecution, AbstractMetisPlugin> result = orchestratorHelper
        .getPreviousExecutionAndPlugin(plugin, datasetId);
    assertNotNull(result);
    assertSame(previousExecution, result.getLeft());
    assertSame(previousPlugin, result.getRight());
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
