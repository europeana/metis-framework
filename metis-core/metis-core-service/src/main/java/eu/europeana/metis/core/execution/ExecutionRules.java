package eu.europeana.metis.core.execution;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
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
  private static final Set<ExecutablePluginType> ALL_EXCEPT_LINK_GROUP = Collections
      .unmodifiableSet(EnumSet.complementOf(EnumSet.of(ExecutablePluginType.LINK_CHECKING)));

  private ExecutionRules() {
    // Private constructor
  }

  /**
   * Retrieve the predecessor plugin for a new plugin of the given plugin type. This method computes
   * the type(s) this predecessor can have according to the execution rules, based on the target
   * plugin type (using {@link #getPredecessorTypes(ExecutablePluginType)}), and then returns the
   * last successful plugin that has one of these types. The enforced predecessor type provides a
   * way to override the computed predecessor type: if provided, any predecessor will be of this
   * type.
   *
   * @param pluginType the type of the new {@link ExecutablePluginType} that is to be executed.
   * @param enforcedPredecessorType the plugin type of the predecessor. Can be null in order to
   * determine the predecessor type according to the execution rules.
   * @param datasetId the dataset ID of the new plugin's dataset.
   * @param workflowExecutionDao {@link WorkflowExecutionDao} to access the corresponding database.
   * @return the {@link AbstractExecutablePlugin} that the pluginType execution will use as a
   * source. Can be null in case the given type does not require a predecessor.
   * @throws PluginExecutionNotAllowed In case a valid predecessor is required, but not found.
   */
  public static AbstractExecutablePlugin getPredecessorPlugin(ExecutablePluginType pluginType,
      ExecutablePluginType enforcedPredecessorType, String datasetId,
      WorkflowExecutionDao workflowExecutionDao) throws PluginExecutionNotAllowed {

    // Determine which predecessor plugin types are permissible.
    final Set<ExecutablePluginType> predecessorTypes = Optional.ofNullable(enforcedPredecessorType)
        .<Set<ExecutablePluginType>>map(EnumSet::of)
        .orElseGet(() -> getPredecessorTypes(pluginType));

    // If no predecessor is required, we are done.
    if (predecessorTypes.isEmpty()) {
      return null;
    }

    // Find the latest successful plugin of this type. If none found, throw exception.
    final AbstractExecutablePlugin predecessor = workflowExecutionDao
        .getLatestSuccessfulPlugin(datasetId, predecessorTypes, true);
    return Optional.ofNullable(predecessor).filter(ExecutionRules::pluginHasSuccessfulRecords)
        .orElseThrow(
            () -> new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED));
  }

  private static boolean pluginHasSuccessfulRecords(AbstractExecutablePlugin plugin) {
    final ExecutionProgress progress = plugin.getExecutionProgress();
    return progress != null && progress.getProcessedRecords() > progress.getErrors();
  }

  /**
   * This method determines what plugin types a plugin of the given type can be based on. This means
   * that the given type can only occur after one of the returned base types.
   *
   * @param pluginType The plugin type for which to return the base types.
   * @return The base types of the given plugin type: those plugin types that a plugin of the given
   * type can be based on. Cannot be null, but can be the empty set in case the plugin type has no
   * predecessors.
   */
  public static Set<ExecutablePluginType> getPredecessorTypes(ExecutablePluginType pluginType) {
    final Set<ExecutablePluginType> predecessorTypes;
    switch (pluginType) {
      case VALIDATION_EXTERNAL:
        predecessorTypes = HARVEST_PLUGIN_GROUP;
        break;
      case TRANSFORMATION:
        predecessorTypes = EnumSet.of(ExecutablePluginType.VALIDATION_EXTERNAL);
        break;
      case VALIDATION_INTERNAL:
        predecessorTypes = EnumSet.of(ExecutablePluginType.TRANSFORMATION);
        break;
      case NORMALIZATION:
        predecessorTypes = EnumSet.of(ExecutablePluginType.VALIDATION_INTERNAL);
        break;
      case ENRICHMENT:
        predecessorTypes = EnumSet.of(ExecutablePluginType.NORMALIZATION);
        break;
      case MEDIA_PROCESS:
        predecessorTypes = EnumSet.of(ExecutablePluginType.ENRICHMENT);
        break;
      case PREVIEW:
        predecessorTypes = EnumSet.of(ExecutablePluginType.MEDIA_PROCESS);
        break;
      case PUBLISH:
        predecessorTypes = EnumSet.of(ExecutablePluginType.PREVIEW);
        break;
      case LINK_CHECKING:
        predecessorTypes = ALL_EXCEPT_LINK_GROUP;
        break;
      case HTTP_HARVEST:
      case OAIPMH_HARVEST:
        predecessorTypes = Collections.emptySet();
        break;
      default:
        throw new IllegalArgumentException("Unrecognized type: " + pluginType);
    }
    return predecessorTypes;
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
