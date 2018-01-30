package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.EnumSet;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ExecutionRules {

  public static final EnumSet<PluginType> harvestPluginGroup = EnumSet
      .of(PluginType.OAIPMH_HARVEST, PluginType.HTTP_HARVEST);
  public static final EnumSet<PluginType> processPluginGroup = EnumSet
      .of(PluginType.VALIDATION_EXTERNAL, PluginType.TRANSFORMATION,
          PluginType.VALIDATION_INTERNAL);
  public static final EnumSet<PluginType> indexPluginGroup = EnumSet
      .of(PluginType.INDEX_TO_PREVIEW);

  public static AbstractMetisPlugin getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      PluginType pluginType, int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    //Get latest FINISHED plugin for datasetId
    if (processPluginGroup.contains(pluginType)) {
      return getLatestFinishedPluginAllowedForExecutionProcess(pluginType, datasetId,
          workflowExecutionDao);
    } else if (indexPluginGroup.contains(pluginType)) {
      // TODO: 29-1-18 Implement when index plugins ready
      return null;
    }
    return null;
  }

  private static AbstractMetisPlugin getLatestFinishedPluginAllowedForExecutionProcess(
      PluginType pluginType,
      int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {

    if (pluginType == PluginType.VALIDATION_EXTERNAL) {
      return workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              harvestPluginGroup);
    } else if (pluginType == PluginType.TRANSFORMATION) {
      return workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(PluginType.VALIDATION_EXTERNAL));

    } else if ((pluginType == PluginType.VALIDATION_INTERNAL)) {
      return workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(PluginType.TRANSFORMATION));
    }
    return null;
  }
}
