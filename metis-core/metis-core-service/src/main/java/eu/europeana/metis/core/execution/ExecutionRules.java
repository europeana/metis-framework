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

  private static final Set<PluginType> HARVEST_PLUGIN_GROUP = EnumSet
      .of(PluginType.OAIPMH_HARVEST, PluginType.HTTP_HARVEST);
  private static final Set<PluginType> PROCESS_PLUGIN_GROUP = EnumSet
      .of(PluginType.VALIDATION_EXTERNAL, PluginType.TRANSFORMATION,
          PluginType.VALIDATION_INTERNAL, PluginType.NORMALIZATION, PluginType.ENRICHMENT,
          PluginType.MEDIA_PROCESS, PluginType.LINK_CHECKING);
  private static final Set<PluginType> INDEX_PLUGIN_GROUP =
      EnumSet.of(PluginType.PREVIEW, PluginType.PUBLISH);

  private ExecutionRules() {
    //Private constructor
  }

  /**
   * Get the latest plugin that is allowed to be run for a plugin that is requested for execution.
   * <p>A pluginType execution must have a source pluginType, except if it's a harvesting plugin.
   * The ordering of the pluginTypes are predefined in code, but an enforcedPluginType can overwrite
   * that, and will try to use the enforcedPluginType as a source, if an execution that has properly
   * finished exists. Executions that are reported as FINISHED but have all records have errors, is
   * not a valid execution as a source.</p>
   *
   * @param pluginType the {@link PluginType} that is to be executed
   * @param enforcedPluginType the {@link PluginType} used to enforce the source pluginType of the
   * execution
   * @param datasetId the dataset identifier to check for
   * @param workflowExecutionDao {@link WorkflowExecutionDao} to access the corresponding database
   * @return the {@link AbstractMetisPlugin} that the pluginType execution will use as a source or
   * null
   */
  public static AbstractMetisPlugin getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      PluginType pluginType, PluginType enforcedPluginType,
      String datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    AbstractMetisPlugin abstractMetisPlugin = null;
    if (enforcedPluginType != null) {
      abstractMetisPlugin = workflowExecutionDao
          .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
              EnumSet.of(enforcedPluginType));
    } else if (PROCESS_PLUGIN_GROUP.contains(pluginType) || INDEX_PLUGIN_GROUP
        .contains(pluginType)) {
      // Get latest FINISHED plugin for datasetId
      abstractMetisPlugin = getLatestFinishedPluginAllowedForExecution(pluginType, datasetId,
          workflowExecutionDao);
    }
    return abstractMetisPlugin;
  }

  private static AbstractMetisPlugin getLatestFinishedPluginAllowedForExecution(
      PluginType pluginType, String datasetId, WorkflowExecutionDao workflowExecutionDao) {

    AbstractMetisPlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = null;

    Set<PluginType> pluginTypesSetThatPluginTypeCanBeBasedOn = getPluginTypesSetThatPluginTypeCanBeBasedOn(pluginType);
    if (pluginTypesSetThatPluginTypeCanBeBasedOn != null) {
      latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
          .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
              pluginTypesSetThatPluginTypeCanBeBasedOn);
    }
    return latestFinishedWorkflowExecutionByDatasetIdAndPluginType;
  }

  /**
   * This method determines what plugin types a plugin of the given type can be based on. This means
   * that the given type can only occur after one of the returned base types.
   * 
   * @param pluginType The plugin type for which to return the base types.
   * @return The base types of the given plugin type: those plugin types that a plugin of the given
   *         type can be based on.
   */
  public static Set<PluginType> getPluginTypesSetThatPluginTypeCanBeBasedOn(PluginType pluginType) {
    Set<PluginType> pluginTypesSetThatPluginTypeCanBeBasedOn = null;
    switch (pluginType) {
      case VALIDATION_EXTERNAL:
        pluginTypesSetThatPluginTypeCanBeBasedOn = HARVEST_PLUGIN_GROUP;
        break;
      case TRANSFORMATION:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.VALIDATION_EXTERNAL);
        break;
      case VALIDATION_INTERNAL:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.TRANSFORMATION);
        break;
      case NORMALIZATION:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.VALIDATION_INTERNAL);
        break;
      case ENRICHMENT:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.NORMALIZATION);
        break;
      case MEDIA_PROCESS:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.ENRICHMENT);
        break;
      case PREVIEW:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.MEDIA_PROCESS);
        break;
      case PUBLISH:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.PREVIEW);
        break;
      case LINK_CHECKING:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(PluginType.VALIDATION_INTERNAL,
            PluginType.NORMALIZATION, PluginType.ENRICHMENT, PluginType.MEDIA_PROCESS,
            PluginType.PREVIEW, PluginType.PUBLISH);
        break;
      default:
        break;
    }
    return pluginTypesSetThatPluginTypeCanBeBasedOn;
  }

  /**
   * @return The plugin types that are of the 'harvesting' kind: they can occur at the beginning of
   *         workflows and don't need another plugin type as base.
   */
  public static Set<PluginType> getHarvestPluginGroup() {
    return EnumSet.copyOf(HARVEST_PLUGIN_GROUP);
  }

  /**
   * @return The plugin types that are of the 'processing' kind.
   */
  public static Set<PluginType> getProcessPluginGroup() {
    return EnumSet.copyOf(PROCESS_PLUGIN_GROUP);
  }

  /**
   * @return The plugin types that are of the 'indexing' kind.
   */
  public static Set<PluginType> getIndexPluginGroup() {
    return EnumSet.copyOf(INDEX_PLUGIN_GROUP);
  }
}
