package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public final class ExecutionRules {

  private static final Set<PluginType> harvestPluginGroup = EnumSet
      .of(PluginType.OAIPMH_HARVEST, PluginType.HTTP_HARVEST);
  private static final Set<PluginType> processPluginGroup = EnumSet
      .of(PluginType.VALIDATION_EXTERNAL, PluginType.TRANSFORMATION,
          PluginType.VALIDATION_INTERNAL);
  private static final Set<PluginType> indexPluginGroup = EnumSet.of(PluginType.INDEX_TO_PREVIEW);

  private ExecutionRules() {
    //Private constructor
  }

  public static AbstractMetisPlugin getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      PluginType pluginType, PluginType enforcedPluginType,
      int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    if (enforcedPluginType != null) {
      return workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(enforcedPluginType));
    } else {
      //Get latest FINISHED plugin for datasetId
      if (processPluginGroup.contains(pluginType)) {
        return getLatestFinishedPluginAllowedForExecutionProcess(pluginType, datasetId,
            workflowExecutionDao);
      } else if (indexPluginGroup.contains(pluginType)) {
        // TODO: 29-1-18 Implement when index plugins ready
        return null;
      }
    }
    return null;
  }

  private static AbstractMetisPlugin getLatestFinishedPluginAllowedForExecutionProcess(
      PluginType pluginType,
      int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {

    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = null;
    if (pluginType == PluginType.VALIDATION_EXTERNAL) {
      latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              harvestPluginGroup);
    } else if (pluginType == PluginType.TRANSFORMATION) {
      latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(PluginType.VALIDATION_EXTERNAL));

    } else if ((pluginType == PluginType.VALIDATION_INTERNAL)) {
      latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(PluginType.TRANSFORMATION));
    }
    return latestFinishedWorkflowExecutionByDatasetIdAndPluginType;
  }

  public static Set<PluginType> getHarvestPluginGroup() {
    return EnumSet.copyOf(harvestPluginGroup);
  }

  public static Set<PluginType> getProcessPluginGroup() {
    return EnumSet.copyOf(processPluginGroup);
  }

  public static Set<PluginType> getIndexPluginGroup() {
    return EnumSet.copyOf(indexPluginGroup);
  }
}
