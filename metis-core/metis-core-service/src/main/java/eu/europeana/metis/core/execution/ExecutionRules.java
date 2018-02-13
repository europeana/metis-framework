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

  /**
   * Get the latest plugin that is allowed to be run for a plugin that is requested for execution.
   * <p>A pluginType execution must have a source pluginType, except if it's a harvesting plugin.
   * The ordering of the pluginTypes are predefined in code, but an enforcedPluginType can overwrite that, and
   * will try to use the enforcedPluginType as a source, if an execution that has properly finished exists.
   * Executions that are reported as FINISHED but have all records have errors, is not a valid execution as a source.</p>
   *
   * @param pluginType the {@link PluginType} that is to be executed
   * @param enforcedPluginType the {@link PluginType} used to enforce the source pluginType of the execution
   * @param datasetId the dataset identifier to check for
   * @param workflowExecutionDao {@link WorkflowExecutionDao} to access the corresponding database
   * @return the {@link AbstractMetisPlugin} that the pluginType execution will use as a source or null
   */
  public static AbstractMetisPlugin getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      PluginType pluginType, PluginType enforcedPluginType,
      int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    AbstractMetisPlugin abstractMetisPlugin = null;
    if (enforcedPluginType != null) {
      abstractMetisPlugin = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(enforcedPluginType));
    } else if (processPluginGroup.contains(pluginType)) { //Get latest FINISHED plugin for datasetId
      abstractMetisPlugin = getLatestFinishedPluginAllowedForExecutionProcess(pluginType, datasetId,
          workflowExecutionDao);
    } else if (indexPluginGroup.contains(pluginType)) {
      // TODO: 29-1-18 Implement when index plugins ready
      abstractMetisPlugin = null;
    }
    return abstractMetisPlugin;
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
