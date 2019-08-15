package eu.europeana.metis.core.execution;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;

/**
 * This class is a utility class that can answer questions related to the validation of workflows.
 */
public class WorkflowUtils {

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

  private final WorkflowExecutionDao workflowExecutionDao;

  /**
   * Constructor.
   * @param workflowExecutionDao {@link WorkflowExecutionDao} to access the database.
   */
  public WorkflowUtils(WorkflowExecutionDao workflowExecutionDao) {
    this.workflowExecutionDao = workflowExecutionDao;
  }

  /**
   * This method validates the workflow plugin sequence. In particular, it checks:
   * <ol>
   * <li>That the workflow is not empty and contains plugins with valid types,</li>
   * <li>That the first plugin is not link checking (except when it is the only plugin),</li>
   * <li>That no two plugins of the same type occur in the workflow,</li>
   * <li>That the first plugin has a valid predecessor plugin in the dataset's history (as defined by
   * {@link #getPredecessorTypes(ExecutablePluginType)}), the type of which can be overridden by the 
   * enforced predecessor type,</li>
   * <li>That all subsequent plugins have a valid predecessor within the workflow (as defined by
   * {@link #getPredecessorTypes(ExecutablePluginType)}),</li>
   * <li>That harvesting plugins have valid URL settings.</li>
   * </ol>
   *
   * @param workflow The workflow to validate.
   * @param enforcedPredecessorType If not null, overrides the predecessor type of the first plugin.
   * @return The predecessor of the first plugin. Or null if no predecessor is required.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} In case the workflow is empty, or contains plugins with
   * invalid types.</li>
   * <li>{@link PluginExecutionNotAllowed} in case the plugin sequence as provided is not
   * allowed.</li>
   * </ul>
   */
  public AbstractExecutablePlugin validateWorkflowPlugins(Workflow workflow,
      ExecutablePluginType enforcedPredecessorType) throws GenericMetisException {

    // Sanity checks: workflow should have a plugin list.
    if (workflow.getMetisPluginsMetadata() == null) {
      throw new BadContentException("Workflow should not be empty.");
    }

    // Compile the list of enabled plugins.
    final List<AbstractExecutablePluginMetadata> plugins = workflow.getMetisPluginsMetadata()
        .stream().filter(AbstractExecutablePluginMetadata::isEnabled).collect(Collectors.toList());
    
    // Sanity checks: workflow should not be empty and all should have a type.
    if (plugins.isEmpty()) {
      throw new BadContentException("Workflow should not be empty.");
    }
    if (plugins.stream().map(AbstractExecutablePluginMetadata::getExecutablePluginType)
        .anyMatch(Objects::isNull)) {
      throw new BadContentException("There are plugins of which the type could not be determined.");
    }

    // Validate and normalize the harvest parameters of harvest plugins (even if not enabled)
    validateAndTrimHarvestParameters(plugins);

    // Check that first plugin is not link checking (except if it is the only plugin)
    if (plugins.size() > 1 && plugins.get(0).getPluginType() == PluginType.LINK_CHECKING) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }

    // Make sure that all plugins (except the first) have a predecessor within the workflow.
    final EnumSet<ExecutablePluginType> previousTypes = EnumSet
        .of(plugins.get(0).getExecutablePluginType());
    for (int i = 1; i < plugins.size(); i++) {

      // Find the permissible predecessors
      final AbstractExecutablePluginMetadata plugin = plugins.get(i);
      final Set<ExecutablePluginType> permissiblePredecessors = getPredecessorTypes(
          plugin.getExecutablePluginType());

      // Check if we have the right predecessor plugin types in the workflow
      final boolean hasNoPredecessor = !permissiblePredecessors.isEmpty() &&
          permissiblePredecessors.stream().noneMatch(previousTypes::contains);
      if (hasNoPredecessor) {
        throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
      }

      // Add the plugin type to those we have seen
      previousTypes.add(plugin.getExecutablePluginType());
    }

    // We should now have seen all types. Make sure that there are no duplicates
    if (previousTypes.size() != plugins.size()) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }
    
    // Check the presence of the predecessor and return it.
    return getPredecessorPlugin(plugins.get(0).getExecutablePluginType(), enforcedPredecessorType,
        workflow.getDatasetId());
  }

  private static void validateAndTrimHarvestParameters(List<AbstractExecutablePluginMetadata> plugins)
      throws BadContentException {
    for (AbstractExecutablePluginMetadata pluginMetadata : plugins) {
      if (pluginMetadata instanceof OaipmhHarvestPluginMetadata) {
        final OaipmhHarvestPluginMetadata oaipmhMetadata = (OaipmhHarvestPluginMetadata) pluginMetadata;
        final URI validatedUri = validateUrl(oaipmhMetadata.getUrl());
        oaipmhMetadata
            .setUrl(new URIBuilder(validatedUri).removeQuery().setFragment(null).toString());
        oaipmhMetadata.setMetadataFormat(oaipmhMetadata.getMetadataFormat() == null ? null
            : oaipmhMetadata.getMetadataFormat().trim());
        oaipmhMetadata.setSetSpec(
            oaipmhMetadata.getSetSpec() == null ? null : oaipmhMetadata.getSetSpec().trim());
      }
      if (pluginMetadata instanceof HTTPHarvestPluginMetadata) {
        final HTTPHarvestPluginMetadata httpMetadata = (HTTPHarvestPluginMetadata) pluginMetadata;
        httpMetadata.setUrl(validateUrl(httpMetadata.getUrl()).toString());
      }
    }
  }

  private static URI validateUrl(String urlString) throws BadContentException {
    if (urlString == null) {
      throw new BadContentException("Harvesting parameters are missing");
    }
    try {
      return new URL(urlString.trim()).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new BadContentException("Harvesting parameters are invalid", e);
    }
  }

  /**
   * Retrieve the predecessor plugin for a plugin of the given plugin type. This method computes the
   * type(s) this predecessor can have according to the execution rules, based on the target plugin
   * type (using {@link #getPredecessorTypes(ExecutablePluginType)}), and then returns the last
   * successful plugin <b>within the given workflow execution</b> that has one of these types. Note
   * that in this context, a plugin that executed with only errors is <b>still</b> counted as
   * successful.
   *
   * @param pluginType the type of the new {@link ExecutablePluginType} that is to be executed.
   * @param workflowExecution The workflow execution in which to look.
   * @return the {@link AbstractExecutablePlugin} that the pluginType execution can use as a source.
   * Can be null in case the given type does not require a predecessor.
   */
  static AbstractExecutablePlugin getPredecessorPlugin(ExecutablePluginType pluginType,
      WorkflowExecution workflowExecution) {

    // If the plugin type does not need a predecessor we are done.
    final Set<ExecutablePluginType> predecessorTypes = getPredecessorTypes(pluginType);
    if (predecessorTypes.isEmpty()) {
      return null;
    }

    // Find the latest successful plugin of one of these types. If none found, throw exception.
    final List<AbstractExecutablePlugin> candidates = workflowExecution.getMetisPlugins().stream()
        .filter(plugin -> plugin instanceof AbstractExecutablePlugin)
        .map(plugin -> (AbstractExecutablePlugin<?>) plugin)
        .filter(plugin -> predecessorTypes
            .contains(plugin.getPluginMetadata().getExecutablePluginType()))
        .filter(plugin -> plugin.getPluginStatus() == PluginStatus.FINISHED)
        .collect(Collectors.toList());
    if (!candidates.isEmpty()) {
      return candidates.get(candidates.size() - 1);
    }

    // If no successful plugin found, throw exception.
    throw new IllegalArgumentException();
  }

  /**
   * Retrieve the predecessor plugin for a new plugin of the given plugin type. This method computes
   * the type(s) this predecessor can have according to the execution rules, based on the target
   * plugin type (using {@link #getPredecessorTypes(ExecutablePluginType)}), and then returns the
   * last successful plugin <b>in the database</b> that has one of these types. The enforced
   * predecessor type provides a way to override the computed predecessor type: if provided, any
   * predecessor will be of this type. Note that in this context, a plugin that executed with only
   * errors is <b>not</b> counted as successful.
   *
   * @param pluginType the type of the new {@link ExecutablePluginType} that is to be executed.
   * @param enforcedPredecessorType If not null, overrides the predecessor type of the plugin.
   * @param datasetId the dataset ID of the new plugin's dataset.
   * @return the {@link AbstractExecutablePlugin} that the pluginType execution will use as a
   * source. Can be null in case the given type does not require a predecessor.
   * @throws PluginExecutionNotAllowed In case a valid predecessor is required, but not found.
   */
  public AbstractExecutablePlugin getPredecessorPlugin(ExecutablePluginType pluginType,
      ExecutablePluginType enforcedPredecessorType, String datasetId)
      throws PluginExecutionNotAllowed {
    
    // If the plugin type does not need a predecessor (even the enforced one) we are done.
    final Set<ExecutablePluginType> defaultPredecessorTypes = getPredecessorTypes(pluginType);
    if (defaultPredecessorTypes.isEmpty()) {
      return null;
    }

    // Determine which predecessor plugin types are permissible (list is never empty).
    final Set<ExecutablePluginType> predecessorTypes = Optional.ofNullable(enforcedPredecessorType)
        .<Set<ExecutablePluginType>>map(EnumSet::of).orElse(defaultPredecessorTypes);

    // Find the latest successful plugin of one of these types. If none found, throw exception.
    final AbstractExecutablePlugin predecessor = workflowExecutionDao
        .getLatestSuccessfulPlugin(datasetId, predecessorTypes, true);
    return Optional.ofNullable(predecessor)
        .filter(WorkflowUtils::pluginHasSuccessfulRecords).orElseThrow(
            () -> new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED));
  }

  private static boolean pluginHasSuccessfulRecords(ExecutablePlugin plugin) {
    final ExecutionProgress progress = plugin.getExecutionProgress();
    return progress != null && progress.getProcessedRecords() > progress.getErrors();
  }

  /**
   * This method determines what plugin types a plugin of the given type can be based on. This means
   * that the given type can only occur after one of the returned base types.
   *
   * @param pluginType The plugin type for which to return the base types.
   * @return The base types of the given plugin type: those plugin types that a plugin of the given
   * type can be based on. Cannot be null, but can be the empty set in case the plugin type requires
   * no predecessor.
   */
  private static Set<ExecutablePluginType> getPredecessorTypes(ExecutablePluginType pluginType) {
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
