package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.Pagination;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ResultList;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.utils.CommonStringValues;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class is a utility class that can answer questions related to the history and evolution of
 * the data.
 */
public class DataEvolutionUtils {

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
   *
   * @param workflowExecutionDao the workflow execution dao
   */
  public DataEvolutionUtils(WorkflowExecutionDao workflowExecutionDao) {
    this.workflowExecutionDao = workflowExecutionDao;
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
      pluginType, WorkflowExecution workflowExecution) {

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
   * method returns null. Null is also returned for a predecessor {@link
   * ExecutablePluginType#PUBLISH} that has at least one successful execution and pluginType {@link
   * ExecutablePluginType#DEPUBLISH}, for which we do not need to perform all the checks since the
   * predecessor may have been superseded by an invalid plugin execution. The enforced predecessor
   * type provides a way to override the computed candidate types (if there are any): if provided,
   * it is the only candidate type, meaning that any resulting predecessor plugin will be of this
   * type.
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
  public PluginWithExecutionId<ExecutablePlugin<?>> computePredecessorPlugin(
      ExecutablePluginType pluginType, ExecutablePluginType enforcedPredecessorType,
      String datasetId) throws PluginExecutionNotAllowed {

    final PluginWithExecutionId<ExecutablePlugin<?>> predecessorPlugin;
    final Set<ExecutablePluginType> defaultPredecessorTypes = getPredecessorTypes(pluginType);

    if (pluginType == ExecutablePluginType.DEPUBLISH) {
      // If a DEPUBLISH Operation is requested we don't link to the predecessor plugin. However,
      // make sure there at least one successful exists (possibly invalidated by later reindex).
      final PluginWithExecutionId<ExecutablePlugin<?>> successfulPublish = workflowExecutionDao
              .getLatestSuccessfulExecutablePlugin(datasetId,
                      EnumSet.of(ExecutablePluginType.PUBLISH), false);
      final boolean hasAtLeastOneSuccessfulPlugin = Optional.ofNullable(successfulPublish)
              .filter(DataEvolutionUtils::pluginHasSuccessfulRecords).isPresent();
      if (!hasAtLeastOneSuccessfulPlugin) {
        throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
      }
      predecessorPlugin = null;
    } else if (defaultPredecessorTypes.isEmpty()) {
      // If the plugin type does not need a predecessor (even the enforced one) we are done.
      predecessorPlugin = null;
    } else {

      // Determine which predecessor plugin types are permissible (list is never empty).
      final Set<ExecutablePluginType> predecessorTypes = Optional
          .ofNullable(enforcedPredecessorType)
          .<Set<ExecutablePluginType>>map(EnumSet::of).orElse(defaultPredecessorTypes);

      // Find the latest successful harvest to compare with. If none exist, throw exception.
      final PluginWithExecutionId<ExecutablePlugin<?>> latestHarvest = Optional
          .ofNullable(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
              HARVEST_PLUGIN_GROUP, true)).orElseThrow(
              () -> new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED));

      // Find the latest successful plugin of each type and filter on existence of successful records.
      final Stream<PluginWithExecutionId<ExecutablePlugin<?>>> latestSuccessfulPlugins = predecessorTypes
          .stream().map(Collections::singleton).map(
              type -> workflowExecutionDao
                  .getLatestSuccessfulExecutablePlugin(datasetId, type, true))
          .filter(Objects::nonNull).filter(DataEvolutionUtils::pluginHasSuccessfulRecords);

      // Sort on finished state, so that the root check occurs as little as possible.
      final Stream<PluginWithExecutionId<ExecutablePlugin<?>>> sortedSuccessfulPlugins =
          latestSuccessfulPlugins.sorted(Comparator.comparing(
              plugin -> Optional.ofNullable(plugin.getPlugin().getFinishedDate())
                  .orElseGet(() -> new Date(Long.MIN_VALUE)),
              Comparator.reverseOrder()));

      // Find the first plugin that satisfies the root check. If none found, throw exception.
      final Predicate<PluginWithExecutionId<ExecutablePlugin<?>>> rootCheck = plugin -> getRootAncestor(
          plugin).equals(latestHarvest);
      predecessorPlugin = sortedSuccessfulPlugins
          .filter(rootCheck).findFirst().orElseThrow(
              () -> new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED));
    }
    return predecessorPlugin;
  }

  private static boolean pluginHasSuccessfulRecords(
      PluginWithExecutionId<ExecutablePlugin<?>> plugin) {
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
  public PluginWithExecutionId<? extends ExecutablePlugin<?>> getRootAncestor(
      PluginWithExecutionId<? extends ExecutablePlugin<?>> plugin) {
    final WorkflowExecution execution = workflowExecutionDao.getById(plugin.getExecutionId());
    final List<Pair<ExecutablePlugin<?>, WorkflowExecution>> evolution =
        compileVersionEvolution(plugin.getPlugin(), execution);
    if (evolution.isEmpty()) {
      return plugin;
    }
    return new PluginWithExecutionId<>(evolution.get(0).getRight(), evolution.get(0).getLeft());
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
      case DEPUBLISH:
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
   * include, this plugin.
   * @param targetPluginExecution The execution in which this target plugin may be found.
   * @return The evolution.
   */
  public List<Pair<ExecutablePlugin<?>, WorkflowExecution>> compileVersionEvolution(
      MetisPlugin<?> targetPlugin, WorkflowExecution targetPluginExecution) {

    // Loop backwards to find the plugin. Don't add the first plugin to the result list.
    Pair<MetisPlugin<?>, WorkflowExecution> currentExecutionAndPlugin = new ImmutablePair<>(
        targetPlugin, targetPluginExecution);
    final ArrayDeque<Pair<ExecutablePlugin<?>, WorkflowExecution>> evolutionSteps = new ArrayDeque<>();
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

  // Not private because of unit tests.
  Pair<MetisPlugin<?>, WorkflowExecution> getPreviousExecutionAndPlugin(MetisPlugin<?> plugin,
      String datasetId) {

    // Check whether we are at the end of the chain.
    final ExecutedMetisPluginId previousPluginId = ExecutedMetisPluginId.forPredecessor(plugin);
    if (previousPluginId == null) {
      return null;
    }

    // Obtain the previous execution and plugin.
    final WorkflowExecution previousExecution = workflowExecutionDao
        .getByTaskExecution(previousPluginId, datasetId);
    final AbstractMetisPlugin<?> previousPlugin = previousExecution == null ? null
        : previousExecution.getMetisPluginWithType(previousPluginId.getPluginType()).orElse(null);
    if (previousExecution == null || previousPlugin == null) {
      return null;
    }

    // Done
    return new ImmutablePair<>(previousPlugin, previousExecution);
  }

  List<PluginWithExecutionId<IndexToPublishPlugin>> getPublishOperationsSortedInversely(String datasetId) {

    // Get all workflows with finished publish plugins (theoretically we can't quite rely on order).
    final Pagination pagination = workflowExecutionDao.createPagination(0, null, true);
    final ResultList<ExecutionDatasetPair> executionsWithPublishOperations = workflowExecutionDao
            .getWorkflowExecutionsOverview(Set.of(datasetId), Set.of(PluginStatus.FINISHED),
                    Set.of(PluginType.PUBLISH), null, null, pagination);

    // Extract all (finished) publish plugins inversely sorted by started date (most recent first).
    final List<PluginWithExecutionId<IndexToPublishPlugin>> publishOperations = new ArrayList<>();
    executionsWithPublishOperations.getResults().stream().map(ExecutionDatasetPair::getExecution)
            .forEach(execution -> execution.getMetisPlugins().stream()
                    .filter(plugin -> plugin instanceof IndexToPublishPlugin)
                    .map(plugin -> (IndexToPublishPlugin) plugin)
                    .map(plugin -> new PluginWithExecutionId<>(execution.getId().toString(), plugin))
                    .forEach(publishOperations::add));
    final Comparator<PluginWithExecutionId<IndexToPublishPlugin>> comparator = Comparator
            .comparing(pair -> pair.getPlugin().getStartedDate());
    publishOperations.sort(comparator.reversed());

    // Done
    return publishOperations;
  }

  /**
   * <p>
   * This method returns a sequence of published harvest increments. This is a list of successive
   * published harvests that must start with the most recent full published harvest and then
   * includes all incremental published harvests (if any) that came after that.
   * </p>
   * <p>
   * Note that a published harvest is a harvest that resulted in a index to publish (i.e. any
   * harvest that is the root predecessor of an index to publish).
   * </p>
   *
   * @param datasetId The dataset ID for which to obtain the chain.
   * @return The chain, in the form of plugin-execution pairs that are ordered chronologically. Is
   * never null, but can be empty if no such chain exists (i.e. the dataset does not have a
   * published harvest or we find an index after the last full harvest that is invalid or did
   * somehow not originate from a harvest).
   */
  public List<PluginWithExecutionId<? extends ExecutablePlugin<?>>> getPublishedHarvestIncrements(
          String datasetId) {

    // Get all publish operations sorted inversely
    final var allPublishOperations = getPublishOperationsSortedInversely(datasetId);

    // Compile a list of all associated harvests (that led to one of these publish operations).
    // Note: we assume that workflows don't cross each other (i.e. an earlier publish cannot have a
    // later harvest). We stop when we find a full harvest (the latest full harvest).
    boolean fullHarvestFound = false;
    final Map<ExecutedMetisPluginId, PluginWithExecutionId<? extends ExecutablePlugin<?>>> resultHarvests = new LinkedHashMap<>();
    for (PluginWithExecutionId<IndexToPublishPlugin> publishOperation : allPublishOperations) {

      // If the publish is not available, we have detected an anomaly. We are done.
      if (MetisPlugin.getDataStatus(publishOperation.getPlugin()) != DataStatus.VALID &&
              MetisPlugin.getDataStatus(publishOperation.getPlugin()) != DataStatus.DEPRECATED) {
        return Collections.emptyList();
      }

      // Get the root harvest and add it to the map.
      final PluginWithExecutionId<? extends ExecutablePlugin<?>> rootHarvest = getRootAncestor(
              publishOperation);

      // If the root harvest is not a harvest, we have detected an anomaly. We are done.
      if (!HARVEST_PLUGIN_GROUP
              .contains(rootHarvest.getPlugin().getPluginMetadata().getExecutablePluginType())) {
        return Collections.emptyList();
      }

      // Add the root harvest to the result (overwrite if already there).
      resultHarvests.put(ExecutedMetisPluginId.forPlugin(rootHarvest.getPlugin()), rootHarvest);

      // If the root harvest is a full harvest, we are done.
      fullHarvestFound = !((rootHarvest.getPlugin() instanceof OaipmhHarvestPlugin)
              && ((OaipmhHarvestPlugin) rootHarvest.getPlugin()).getPluginMetadata()
              .isIncrementalHarvest());
      if (fullHarvestFound) {
        break;
      }
    }

    // Done. Sort and return the harvests in the right order (just reversing the result).
    if (!fullHarvestFound) {
      return Collections.emptyList();
    }
    final List<PluginWithExecutionId<? extends ExecutablePlugin<?>>> result = new ArrayList<>(
            resultHarvests.values());
    Collections.reverse(result);
    return result;
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
