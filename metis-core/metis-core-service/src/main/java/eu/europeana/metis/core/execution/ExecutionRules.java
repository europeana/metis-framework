package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a utility class that can answer questions about which workflow steps (plugins) can
 * occur before/after which.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public final class ExecutionRules {

  private static final Set<ExecutablePluginType> HARVEST_PLUGIN_GROUP = Collections.unmodifiableSet(
      EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST, ExecutablePluginType.HTTP_HARVEST));
  private static final Set<ExecutablePluginType> PROCESS_PLUGIN_GROUP = Collections.unmodifiableSet(
      EnumSet.of(ExecutablePluginType.VALIDATION_EXTERNAL, ExecutablePluginType.TRANSFORMATION,
          ExecutablePluginType.VALIDATION_INTERNAL, ExecutablePluginType.NORMALIZATION,
          ExecutablePluginType.ENRICHMENT, ExecutablePluginType.MEDIA_PROCESS,
          ExecutablePluginType.LINK_CHECKING));
  private static final Set<ExecutablePluginType> INDEX_PLUGIN_GROUP = Collections
      .unmodifiableSet(EnumSet.of(ExecutablePluginType.PREVIEW, ExecutablePluginType.PUBLISH));
  private static final Set<ExecutablePluginType> ALL_EXCEPT_LINK_GROUP;

  static {
    Set<ExecutablePluginType> mergedSet = new HashSet<>();
    mergedSet.addAll(HARVEST_PLUGIN_GROUP);
    mergedSet.addAll(PROCESS_PLUGIN_GROUP);
    mergedSet.addAll(INDEX_PLUGIN_GROUP);
    mergedSet.remove(ExecutablePluginType.LINK_CHECKING);
    ALL_EXCEPT_LINK_GROUP = Collections.unmodifiableSet(mergedSet);
  }

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
   * @param pluginType the {@link ExecutablePluginType} that is to be executed
   * @param enforcedPluginType the {@link ExecutablePluginType} used to enforce the source
   * pluginType of the execution
   * @param datasetId the dataset identifier to check for
   * @param workflowExecutionDao {@link WorkflowExecutionDao} to access the corresponding database
   * @return the {@link AbstractExecutablePlugin} that the pluginType execution will use as a source
   * or null
   */
  public static AbstractExecutablePlugin getLatestFinishedPluginIfRequestedPluginAllowedForExecution(
      ExecutablePluginType pluginType, ExecutablePluginType enforcedPluginType, String datasetId,
      WorkflowExecutionDao workflowExecutionDao) {
    AbstractExecutablePlugin plugin = null;
    if (enforcedPluginType != null) {
      plugin = workflowExecutionDao
          .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
              EnumSet.of(enforcedPluginType), true);
    } else if (PROCESS_PLUGIN_GROUP.contains(pluginType) || INDEX_PLUGIN_GROUP
        .contains(pluginType)) {
      // Get latest FINISHED plugin for datasetId
      plugin = getLatestFinishedPluginAllowedForExecution(pluginType, datasetId,
          workflowExecutionDao);
      // TODO: 12-3-19 The following code should be removed after DPS technical gap applied. At about September 2019
      // If pluginType is MEDIA_PROCESS and there is no latest plugin allowed, therefore abstractMetisPlugin == null,
      // check latest successful OAIPMH_HARVEST and if that was based on the europeana endpoint during migration, then
      // we return that instead.
      if (pluginType == ExecutablePluginType.MEDIA_PROCESS && plugin == null) {
        final AbstractExecutablePlugin latestOaiPlugin = workflowExecutionDao
            .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
                EnumSet.of(ExecutablePluginType.OAIPMH_HARVEST), true);
        if (latestOaiPlugin != null && "https://oai-pmh.eanadev.org/oai"
            .equalsIgnoreCase(((OaipmhHarvestPluginMetadata) latestOaiPlugin
                .getPluginMetadata()).getUrl().trim())) {
          plugin = latestOaiPlugin;
        }
      }
    }
    return plugin;
  }

  private static AbstractExecutablePlugin getLatestFinishedPluginAllowedForExecution(
      ExecutablePluginType pluginType, String datasetId,
      WorkflowExecutionDao workflowExecutionDao) {

    AbstractExecutablePlugin latestFinishedWorkflowExecutionByDatasetIdAndPluginType = null;

    Set<ExecutablePluginType> pluginTypesSetThatPluginTypeCanBeBasedOn = getPluginTypesSetThatPluginTypeCanBeBasedOn(
        pluginType);
    if (!pluginTypesSetThatPluginTypeCanBeBasedOn.isEmpty()) {
      latestFinishedWorkflowExecutionByDatasetIdAndPluginType = workflowExecutionDao
          .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId,
              pluginTypesSetThatPluginTypeCanBeBasedOn, true);
    }
    return latestFinishedWorkflowExecutionByDatasetIdAndPluginType;
  }

  /**
   * This method determines what plugin types a plugin of the given type can be based on. This means
   * that the given type can only occur after one of the returned base types.
   *
   * @param pluginType The plugin type for which to return the base types.
   * @return The base types of the given plugin type: those plugin types that a plugin of the given
   * type can be based on. Cannot be null, but can be the empty set.
   */
  public static Set<ExecutablePluginType> getPluginTypesSetThatPluginTypeCanBeBasedOn(
      ExecutablePluginType pluginType) {
    final Set<ExecutablePluginType> pluginTypesSetThatPluginTypeCanBeBasedOn;
    switch (pluginType) {
      case VALIDATION_EXTERNAL:
        pluginTypesSetThatPluginTypeCanBeBasedOn = HARVEST_PLUGIN_GROUP;
        break;
      case TRANSFORMATION:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet
            .of(ExecutablePluginType.VALIDATION_EXTERNAL);
        break;
      case VALIDATION_INTERNAL:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(ExecutablePluginType.TRANSFORMATION);
        break;
      case NORMALIZATION:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet
            .of(ExecutablePluginType.VALIDATION_INTERNAL);
        break;
      case ENRICHMENT:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(ExecutablePluginType.NORMALIZATION);
        break;
      case MEDIA_PROCESS:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(ExecutablePluginType.ENRICHMENT);
        break;
      case PREVIEW:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(ExecutablePluginType.MEDIA_PROCESS);
        break;
      case PUBLISH:
        pluginTypesSetThatPluginTypeCanBeBasedOn = EnumSet.of(ExecutablePluginType.PREVIEW);
        break;
      case LINK_CHECKING:
        pluginTypesSetThatPluginTypeCanBeBasedOn = ALL_EXCEPT_LINK_GROUP;
        break;
      default:
        pluginTypesSetThatPluginTypeCanBeBasedOn = Collections.emptySet();
        break;
    }
//    if (!pluginTypesSetThatPluginTypeCanBeBasedOn.isEmpty()) {
//      pluginTypesSetThatPluginTypeCanBeBasedOn.add(ExecutablePluginType.LINK_CHECKING);
//    }
    return pluginTypesSetThatPluginTypeCanBeBasedOn;
  }

  /**
   * @return The plugin types that are of the 'harvesting' kind: they can occur at the beginning of
   * workflows and don't need another plugin type as base.
   */
  public static Set<ExecutablePluginType> getHarvestPluginGroup() {
    return HARVEST_PLUGIN_GROUP;
  }

  /**
   * @return The plugin types that are of the 'processing' kind.
   */
  public static Set<ExecutablePluginType> getProcessPluginGroup() {
    return PROCESS_PLUGIN_GROUP;
  }

  /**
   * @return The plugin types that are of the 'indexing' kind.
   */
  public static Set<ExecutablePluginType> getIndexPluginGroup() {
    return INDEX_PLUGIN_GROUP;
  }

  /**
   * @return The plugin types that are of the 'link checking' kind.
   */
  public static Set<ExecutablePluginType> getAllExceptLinkGroup() {
    return ALL_EXCEPT_LINK_GROUP;
  }
}
