package eu.europeana.metis.core.dao;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.util.DepublishedRecordSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.util.CollectionUtils;

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
  private final DepublishRecordIdDao depublishRecordIdDao;

  /**
   * Constructor.
   *
   * @param workflowExecutionDao the workflow execution dao
   * @param depublishRecordIdDao the depublication record id dao
   */
  public WorkflowUtils(WorkflowExecutionDao workflowExecutionDao,
      DepublishRecordIdDao depublishRecordIdDao) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.depublishRecordIdDao = depublishRecordIdDao;
  }

  /**
   * This method validates the workflow plugin sequence. In particular, it checks:
   * <ol>
   * <li>That the workflow is not empty and contains plugins with valid types,</li>
   * <li>That the first plugin is not link checking (except when it is the only plugin),</li>
   * <li>That no two plugins of the same type occur in the workflow,</li>
   * <li>That if depublish is enabled no other plugins are allowed in the workflow,</li>
   * <li>That the first plugin has a valid predecessor plugin in the dataset's history (as defined by
   * {@link #getPredecessorTypes(ExecutablePluginType)}), the type of which can be overridden by the
   * enforced predecessor type, and the root plugin (i.e. harvest) of which is equal to the latest
   * successful harvest (i.e. no old data should be processed after new data has been introduced),</li>
   * <li>That all subsequent plugins have a valid predecessor within the workflow (as defined by
   * {@link #getPredecessorTypes(ExecutablePluginType)}),</li>
   * <li>That harvesting plugins have valid URL settings.</li>
   * </ol>
   *
   * @param workflow The workflow to validate.
   * @param enforcedPredecessorType If not null, overrides the predecessor type of the first
   * plugin.
   * @return The predecessor of the first plugin. Or null if no predecessor is required.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} In case the workflow is empty, or contains plugins with
   * invalid types.</li>
   * <li>{@link PluginExecutionNotAllowed} in case the plugin sequence as provided is not
   * allowed.</li>
   * </ul>
   */
  public PluginWithExecutionId<ExecutablePlugin> validateWorkflowPlugins(Workflow workflow,
      ExecutablePluginType enforcedPredecessorType) throws GenericMetisException {

    // Sanity checks: workflow should have a plugin list.
    if (workflow.getMetisPluginsMetadata() == null) {
      throw new BadContentException("Workflow should not be empty.");
    }

    // Compile the list of enabled enabledPlugins.
    final List<AbstractExecutablePluginMetadata> enabledPlugins = workflow.getMetisPluginsMetadata()
        .stream().filter(AbstractExecutablePluginMetadata::isEnabled).collect(Collectors.toList());

    // Sanity checks: workflow should not be empty and all should have a type.
    if (enabledPlugins.isEmpty()) {
      throw new BadContentException("Workflow should not be empty.");
    }
    if (enabledPlugins.stream().map(AbstractExecutablePluginMetadata::getExecutablePluginType)
        .anyMatch(Objects::isNull)) {
      throw new BadContentException(
          "There are enabledPlugins of which the type could not be determined.");
    }

    // If depublish requested, make sure it's the only plugin in the workflow
    final Optional<DepublishPluginMetadata> depublishPluginMetadata = enabledPlugins.stream()
        .filter(plugin -> plugin.getExecutablePluginType().toPluginType() == PluginType.DEPUBLISH)
        .map(plugin -> (DepublishPluginMetadata) plugin).findFirst();
    if (enabledPlugins.size() > 1 && depublishPluginMetadata.isPresent()) {
      throw new BadContentException(
          "If DEPUBLISH plugin enabled, no other enabledPlugins are allowed.");
    }

    // If record depublication requested, check if there are pending record ids in the db
    if (depublishPluginMetadata.isPresent() && !depublishPluginMetadata.get()
        .isDatasetDepublish()) {
      final Set<String> pendingDepublicationIds = depublishRecordIdDao
          .getAllDepublishRecordIdsWithStatus(workflow.getDatasetId(),
              DepublishedRecordSortField.DEPUBLICATION_STATE, SortDirection.ASCENDING,
              DepublicationStatus.PENDING_DEPUBLICATION);
      if (CollectionUtils.isEmpty(pendingDepublicationIds)) {
        throw new BadContentException(
            "Record depublication requested but there are no pending depublication record ids in the db");
      }
    }

    // Validate and normalize the harvest parameters of harvest enabledPlugins (even if not enabled)
    validateAndTrimHarvestParameters(enabledPlugins);

    // Check that first plugin is not link checking (except if it is the only plugin)
    if (enabledPlugins.size() > 1
        && enabledPlugins.get(0).getPluginType() == PluginType.LINK_CHECKING) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }

    // Make sure that all enabledPlugins (except the first) have a predecessor within the workflow.
    final EnumSet<ExecutablePluginType> previousTypesInWorkflow = EnumSet
        .of(enabledPlugins.get(0).getExecutablePluginType());
    for (int i = 1; i < enabledPlugins.size(); i++) {

      // Find the permissible predecessors
      final ExecutablePluginType pluginType = enabledPlugins.get(i).getExecutablePluginType();
      final Set<ExecutablePluginType> permissiblePredecessors = getPredecessorTypes(pluginType);

      // Check if we have the right predecessor plugin types in the workflow
      final boolean hasNoPredecessor = !permissiblePredecessors.isEmpty() &&
          permissiblePredecessors.stream().noneMatch(previousTypesInWorkflow::contains);
      if (hasNoPredecessor) {
        throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
      }

      // Add the plugin type to those we have seen
      previousTypesInWorkflow.add(pluginType);
    }

    // We should now have seen all types. Make sure that there are no duplicates
    if (previousTypesInWorkflow.size() != enabledPlugins.size()) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }

    // Check the presence of the predecessor and return it.
    return computePredecessorPlugin(enabledPlugins.get(0).getExecutablePluginType(),
        enforcedPredecessorType, workflow.getDatasetId());
  }

  private static void validateAndTrimHarvestParameters(
      List<AbstractExecutablePluginMetadata> enabledPlugins)
      throws BadContentException {
    for (AbstractExecutablePluginMetadata pluginMetadata : enabledPlugins) {
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
   * <p>
   * Compute the predecessor plugin for a new plugin of the given plugin type.
   * </p>
   *
   * <p>
   * This method first computes the candidate type(s) this predecessor can have according to the
   * execution rules, based on the given target plugin type (using {@link
   * #getPredecessorTypes(ExecutablePluginType)}). If no predecessor is required for this type, this
   * method returns null.
   * </p>
   *
   * <p>
   * Then the method returns the plugin <b>within the given workflow execution</b> that:
   * <ol>
   * <li>Has one of these candidate types,</li>
   * <li>Is the latest plugin of its kind, and</li>
   * <li>Was executed successfully.</li>
   * </ol>
   * </p>
   *
   * <p>
   * Note that in this context, as mentioned above, a plugin that executed with only errors is
   * <b>still</b> counted as successful. Also, there is <b>no</b> requirement that the latest
   * successful harvest plugin is an ancestor of the resulting plugin.
   * </p>
   *
   * @param pluginType the type of the new {@link ExecutablePluginType} that is to be executed.
   * @param workflowExecution The workflow execution in which to look.
   * @return the {@link AbstractExecutablePlugin} that the pluginType execution can use as a source.
   * Can be null in case the given type does not require a predecessor.
   */
  public static AbstractExecutablePlugin computePredecessorPlugin(ExecutablePluginType
      pluginType,
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
   * <p>
   * Compute the predecessor plugin for a new plugin of the given plugin type.
   * </p>
   *
   * <p>
   * This method first computes the candidate type(s) this predecessor can have according to the
   * execution rules, based on the given target plugin type (using {@link
   * #getPredecessorTypes(ExecutablePluginType)}). If no predecessor is required for this type, this
   * method returns null. The enforced predecessor type provides a way to override the computed
   * candidate types (if there are any): if provided, it is the only candidate type, meaning that
   * any resulting predecessor plugin will be of this type.
   * </p>
   *
   * <p>
   * Then the method returns the plugin <b>in the database</b> that:
   * <ol>
   * <li>Has one of these candidate types, or the enforced predecessor type if provided,</li>
   * <li>Is the latest plugin of its kind,</li>
   * <li>Was executed successfully (with at least one successful record), and</li>
   * <li>Has the latest successful harvest plugin as its ancestor.</li>
   * </ol>
   * </p>
   *
   * <p>
   * Note that in this context, as mentioned above, a plugin that executed with only errors is
   * <b>not</b> counted as successful. Also, it is <b>required</b> that the latest successful
   * harvest plugin is an ancestor of the resulting plugin.
   * </p>
   *
   * @param pluginType the type of the new {@link ExecutablePluginType} that is to be executed.
   * @param enforcedPredecessorType If not null, overrides the predecessor type of the plugin.
   * @param datasetId the dataset ID of the new plugin's dataset.
   * @return the {@link ExecutablePlugin} that the pluginType execution will use as a source. Can be
   * null in case the given type does not require a predecessor.
   * @throws PluginExecutionNotAllowed In case a valid predecessor is required, but not found.
   */
  public PluginWithExecutionId<ExecutablePlugin> computePredecessorPlugin(
      ExecutablePluginType pluginType, ExecutablePluginType enforcedPredecessorType,
      String datasetId) throws PluginExecutionNotAllowed {

    // If the plugin type does not need a predecessor (even the enforced one) we are done.
    final Set<ExecutablePluginType> defaultPredecessorTypes = getPredecessorTypes(pluginType);
    if (defaultPredecessorTypes.isEmpty()) {
      return null;
    }

    // Determine which predecessor plugin types are permissible (list is never empty).
    final Set<ExecutablePluginType> predecessorTypes = Optional
        .ofNullable(enforcedPredecessorType)
        .<Set<ExecutablePluginType>>map(EnumSet::of).orElse(defaultPredecessorTypes);

    // Find the latest successful harvest to compare with. If none exist, throw exception.
    final PluginWithExecutionId<ExecutablePlugin> latestHarvest = Optional
        .ofNullable(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
            HARVEST_PLUGIN_GROUP, true)).orElseThrow(
            () -> new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED));

    // Find the latest successful plugin of each type and filter on existence of successful records.
    final Stream<PluginWithExecutionId<ExecutablePlugin>> latestSuccessfulPlugins = predecessorTypes
        .stream().map(Collections::singleton).map(
            type -> workflowExecutionDao
                .getLatestSuccessfulExecutablePlugin(datasetId, type, true))
        .filter(Objects::nonNull).filter(WorkflowUtils::pluginHasSuccessfulRecords);

    // Sort on finished state, so that the root check occurs as little as possible.
    final Stream<PluginWithExecutionId<ExecutablePlugin>> sortedSuccessfulPlugins =
        latestSuccessfulPlugins.sorted(Comparator.comparing(
            plugin -> Optional.ofNullable(plugin.getPlugin().getFinishedDate())
                .orElseGet(() -> new Date(Long.MIN_VALUE)),
            Comparator.reverseOrder()));

    // Find the first plugin that satisfies the root check. If none found, throw exception.
    final Predicate<PluginWithExecutionId<ExecutablePlugin>> rootCheck = plugin -> getRootAncestor(
        plugin).equals(latestHarvest);
    return sortedSuccessfulPlugins.filter(rootCheck).findFirst().orElseThrow(
        () -> new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED));
  }

  private static boolean pluginHasSuccessfulRecords(
      PluginWithExecutionId<ExecutablePlugin> plugin) {
    final ExecutionProgress progress = plugin.getPlugin().getExecutionProgress();
    return progress != null && progress.getProcessedRecords() > progress.getErrors();
  }

  /**
   * Obtains the root ancestor plugin of the given plugin. This returns the one ancestor plugin that
   * does not itself have a predecessor.
   *
   * @param plugin The plugin for which to find the root ancestor.
   * @return The root ancestor. Is not null.
   */
  public PluginWithExecutionId<ExecutablePlugin> getRootAncestor(
      PluginWithExecutionId<ExecutablePlugin> plugin) {
    final WorkflowExecution execution = workflowExecutionDao.getById(plugin.getExecutionId());
    final List<Pair<ExecutablePlugin, WorkflowExecution>> evolution =
        compileVersionEvolution(plugin.getPlugin(), execution);
    return evolution.stream().findFirst()
        .map(pair -> new PluginWithExecutionId<>(pair.getRight(), pair.getLeft()))
        .orElse(plugin);
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
      case DEPUBLISH:
        predecessorTypes = EnumSet.of(ExecutablePluginType.PUBLISH);
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
   * Get the evolution of the records from when they were first imported until (and excluding) the
   * target version.
   *
   * @param targetPlugin The target for compiling the evolution: the result will lead to, but not
   * inclide, this plugin.
   * @param targetPluginExecution The execution in which this target plugin may be found.
   * @return The evolution.
   */
  public List<Pair<ExecutablePlugin, WorkflowExecution>> compileVersionEvolution(
      MetisPlugin targetPlugin, WorkflowExecution targetPluginExecution) {

    // Loop backwards to find the plugin. Don't add the first plugin to the result list.
    Pair<MetisPlugin, WorkflowExecution> currentExecutionAndPlugin = new ImmutablePair<>(
        targetPlugin, targetPluginExecution);
    final ArrayDeque<Pair<ExecutablePlugin, WorkflowExecution>> evolutionSteps = new ArrayDeque<>();
    while (true) {

      // Move to the previous execution: stop when we have none or it is not executable.
      currentExecutionAndPlugin = getPreviousExecutionAndPlugin(
          currentExecutionAndPlugin.getLeft(),
          currentExecutionAndPlugin.getRight().getDatasetId());
      if (currentExecutionAndPlugin == null || !(currentExecutionAndPlugin
          .getLeft() instanceof ExecutablePlugin)) {
        break;
      }

      // Add step to the beginning of the list.
      evolutionSteps.addFirst(new ImmutablePair<>(
          (ExecutablePlugin<?>) currentExecutionAndPlugin.getLeft(),
          currentExecutionAndPlugin.getRight()));
    }

    // Done
    return new ArrayList<>(evolutionSteps);
  }

  Pair<MetisPlugin, WorkflowExecution> getPreviousExecutionAndPlugin(MetisPlugin plugin,
      String datasetId) {

    // Check whether we are at the end of the chain.
    final Date previousPluginTimestamp = plugin.getPluginMetadata()
        .getRevisionTimestampPreviousPlugin();
    final PluginType previousPluginType = PluginType.getPluginTypeFromEnumName(
        plugin.getPluginMetadata().getRevisionNamePreviousPlugin());
    if (previousPluginTimestamp == null || previousPluginType == null) {
      return null;
    }

    // Obtain the previous execution and plugin.
    final WorkflowExecution previousExecution = workflowExecutionDao
        .getByTaskExecution(previousPluginTimestamp, previousPluginType, datasetId);
    final AbstractMetisPlugin previousPlugin = previousExecution == null ? null
        : previousExecution.getMetisPluginWithType(previousPluginType).orElse(null);
    if (previousExecution == null || previousPlugin == null) {
      return null;
    }

    // Done
    return new ImmutablePair<>(previousPlugin, previousExecution);
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
