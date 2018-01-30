package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ExecutionRules {

  private static final EnumSet<PluginType> harvestPluginGroup = EnumSet
      .of(PluginType.OAIPMH_HARVEST, PluginType.HTTP_HARVEST);
  private static final EnumSet<PluginType> processPluginGroup = EnumSet
      .of(PluginType.VALIDATION_EXTERNAL, PluginType.TRANSFORMATION,
          PluginType.VALIDATION_INTERNAL);
  private static final EnumSet<PluginType> indexPluginGroup = EnumSet
      .of(PluginType.INDEX_TO_PREVIEW);

  public static boolean isPluginAllowedForExecution(PluginType pluginType, int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    //Get latest FINISHED plugin for datasetId
    if (harvestPluginGroup.contains(pluginType)) {
      return true;
    } else if (processPluginGroup.contains(pluginType)) {
      getLatestFinishedPluginAllowedForExecution(pluginType, datasetId, workflowExecutionDao);
    } else if (indexPluginGroup.contains(pluginType)) {
      // TODO: 29-1-18 Implement when index plugins ready
      return false;
    }

    return false;
  }

  private static AbstractMetisPlugin getLatestFinishedPluginAllowedForExecution(PluginType pluginType,
      int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    List<AbstractMetisPlugin> latestFinishedPlugins = new ArrayList<>();

    if (pluginType == PluginType.VALIDATION_EXTERNAL) {
      AbstractMetisPlugin latestFinishedHarvestPlugin = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              harvestPluginGroup);
      if (latestFinishedHarvestPlugin != null) {
        latestFinishedPlugins.add(latestFinishedHarvestPlugin);
      }
    } else if (pluginType == PluginType.TRANSFORMATION) {
      AbstractMetisPlugin latestFinishedHarvestPlugin = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(PluginType.VALIDATION_EXTERNAL));
      if (latestFinishedHarvestPlugin != null) {
        latestFinishedPlugins.add(latestFinishedHarvestPlugin);
      }

      AbstractMetisPlugin latestFinishedValidationExternalPlugin = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              harvestPluginGroup);
      if (latestFinishedHarvestPlugin != null) {
        latestFinishedPlugins.add(latestFinishedValidationExternalPlugin);
      }

    } else if ((pluginType == PluginType.VALIDATION_INTERNAL)) {
      AbstractMetisPlugin latestFinishedTransformationPlugin = workflowExecutionDao
          .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(datasetId,
              EnumSet.of(PluginType.TRANSFORMATION));
      if (latestFinishedTransformationPlugin != null) {
        latestFinishedPlugins.add(latestFinishedTransformationPlugin);
      }
    }

    latestFinishedPlugins
        .sort(Comparator.comparing(AbstractMetisPlugin::getFinishedDate).reversed());
    if (latestFinishedPlugins.size() > 0) {
      return latestFinishedPlugins.get(0);
    }
    return null;
  }
}
