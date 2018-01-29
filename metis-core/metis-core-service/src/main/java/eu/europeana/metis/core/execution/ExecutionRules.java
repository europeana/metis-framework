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
public class ExecutionRules {

  private static final Set<PluginType> harvestPluginGroup = EnumSet
      .of(PluginType.OAIPMH_HARVEST, PluginType.HTTP_HARVEST);
  private static final Set<PluginType> processPluginGroup = EnumSet
      .of(PluginType.VALIDATION_EXTERNAL, PluginType.TRANSFORMATION,
          PluginType.VALIDATION_INTERNAL);
  private static final Set<PluginType> indexPluginGroup = EnumSet.of(PluginType.INDEX_TO_PREVIEW);

  public static boolean isPluginAllowedForExecution(PluginType pluginType, int datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    //Get latest FINISHED plugin for datasetId
//    workflowExecutionDao.getLatestFinishedPluginWorkflowExecutionByDatasetId(datasetId)
    AbstractMetisPlugin abstractMetisPlugin = null;

    if (harvestPluginGroup.contains(pluginType)) {
      return true;
    } else if (processPluginGroup.contains(pluginType)) {
      isProcessPluginAllowedForExecution(pluginType, abstractMetisPlugin);
    } else if (indexPluginGroup.contains(pluginType)) {
      // TODO: 29-1-18 Implement when index plugins ready
      return false;
    }

    return false;
  }

  private static boolean isProcessPluginAllowedForExecution(PluginType pluginType,
      AbstractMetisPlugin abstractMetisPlugin) {
    if (pluginType == PluginType.VALIDATION_EXTERNAL) {
      return harvestPluginGroup.contains(abstractMetisPlugin.getPluginType());
    } else if (pluginType == PluginType.TRANSFORMATION) {
      return harvestPluginGroup.contains(abstractMetisPlugin.getPluginType())
          || abstractMetisPlugin.getPluginType() == PluginType.VALIDATION_EXTERNAL;
    } else if ((pluginType == PluginType.VALIDATION_INTERNAL)) {
      return harvestPluginGroup.contains(abstractMetisPlugin.getPluginType())
          || abstractMetisPlugin.getPluginType() == PluginType.TRANSFORMATION
          || abstractMetisPlugin.getPluginType() == PluginType.VALIDATION_EXTERNAL;
    }
    return false;
  }
}
