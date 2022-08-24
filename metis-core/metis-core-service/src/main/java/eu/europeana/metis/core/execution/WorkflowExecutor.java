package eu.europeana.metis.core.execution;

import static eu.europeana.metis.network.ExternalRequestUtil.UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS;
import static java.lang.Thread.currentThread;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.ExecutedMetisPluginId;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.InvalidIndexPluginException;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractIndexPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DpsTaskSettings;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin.MonitorResult;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.ThrottlingValues;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.network.ExternalRequestUtil;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a {@link Callable} class that accepts a {@link WorkflowExecution}. It starts that
 * WorkflowExecution given to it and will continue monitoring and updating its progress until it
 * ends either by user interaction or by the end of the Workflow. When the WorkflowExecution is
 * received there is a chance that the execution is already being handled from another
 * WorkflowExecutor in another instance and if that is the case the WorkflowExecution will be
 * dropped.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class WorkflowExecutor implements Callable<Pair<WorkflowExecution, Boolean>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutor.class);
  private static final String EXECUTION_ERROR_PREFIX = "Execution of external task presented with an error. ";
  private static final String MONITOR_ERROR_PREFIX = "An error occurred while monitoring the external task. ";
  private static final String POSTPROCESS_ERROR_PREFIX = "An error occurred while post-processing the external task. ";
  private static final String TRIGGER_ERROR_PREFIX = "An error occurred while triggering the external task. ";
  private static final String DETAILED_EXCEPTION_FORMAT = "%s%nDetailed exception:%s";

  protected static final int MAX_CANCEL_OR_MONITOR_FAILURES = 10;

  private final SemaphoresPerPluginManager semaphoresPerPluginManager;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowPostProcessor workflowPostProcessor;
  private final int monitorCheckIntervalInSecs;
  private final long periodOfNoProcessedRecordsChangeInSeconds;
  private final DpsClient dpsClient;
  private final String ecloudBaseUrl;
  private final String ecloudProvider;
  private final String metisCoreBaseUrl;
  private WorkflowExecution workflowExecution;
  private final ThrottlingValues throttlingValues;

  WorkflowExecutor(WorkflowExecution workflowExecution, PersistenceProvider persistenceProvider,
      WorkflowExecutionSettings workflowExecutionSettings) {
    this.workflowExecution = workflowExecution;
    this.semaphoresPerPluginManager = persistenceProvider.getSemaphoresPerPluginManager();
    this.workflowExecutionDao = persistenceProvider.getWorkflowExecutionDao();
    this.workflowPostProcessor = persistenceProvider.getWorkflowPostProcessor();
    this.dpsClient = persistenceProvider.getDpsClient();
    this.monitorCheckIntervalInSecs = workflowExecutionSettings.getDpsMonitorCheckIntervalInSecs();
    this.periodOfNoProcessedRecordsChangeInSeconds = TimeUnit.MINUTES
        .toSeconds(workflowExecutionSettings.getPeriodOfNoProcessedRecordsChangeInMinutes());
    this.ecloudBaseUrl = workflowExecutionSettings.getEcloudBaseUrl();
    this.ecloudProvider = workflowExecutionSettings.getEcloudProvider();
    this.metisCoreBaseUrl = workflowExecutionSettings.getMetisCoreBaseUrl();
    this.throttlingValues = workflowExecutionSettings.getThrottlingValues();
  }

  @Override
  public Pair<WorkflowExecution, Boolean> call() {
    // Perform the work - run the workflow.
    LOGGER.info("workflowExecutionId: {}, priority {} - Starting workflow execution",
        workflowExecution.getId(), workflowExecution.getWorkflowPriority());
    final Pair<Date, Boolean> didPluginRunDatePair = runInqueueOrRunningStateWorkflowExecution();
    final Date finishDate = didPluginRunDatePair.getLeft();
    final Boolean didPluginsRun = didPluginRunDatePair.getRight();

    // Process the results if we were not interrupted
    if (!currentThread().isInterrupted()) {
      if (finishDate == null && workflowExecutionDao.isCancelling(workflowExecution.getId())) {
        // If the workflow was cancelled before it had the chance to finish, we cancel all remaining
        // plugins.
        workflowExecution.setWorkflowAndAllQualifiedPluginsToCancelled();
        // Make sure the cancelledBy information is not lost
        String cancelledBy = workflowExecutionDao.getById(workflowExecution.getId().toString())
            .getCancelledBy();
        workflowExecution.setCancelledBy(cancelledBy);
        LOGGER.info("workflowExecutionId: {} - Cancelled running workflow execution",
            workflowExecution.getId());
      } else if (finishDate == null && didPluginsRun) {
        // One plugin failed
        workflowExecution.checkAndSetAllRunningAndInqueuePluginsToCancelledIfOnePluginHasFailed();
      } else if (finishDate == null) {
        // A plugin was not allowed to run because of no slot space
        // Increase priority for this execution
        workflowExecution.setWorkflowPriority(workflowExecution.getWorkflowPriority() + 1);
        LOGGER.info("workflowExecution: {} - Stop workflow execution a plugin was not allowed to "
            + "run(priority increased)", workflowExecution.getId());
      } else {
        // If the workflow finished successfully, we record this.
        workflowExecution.setFinishedDate(finishDate);
        workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
        workflowExecution.setCancelling(false);
        LOGGER.info("workflowExecutionId: {} - Finished workflow execution",
            workflowExecution.getId());
      }
    }

    // The only full update is used here. The rest of the execution uses partial updates to avoid
    // losing the cancelling state field
    workflowExecutionDao.update(workflowExecution);
    return new ImmutablePair<>(workflowExecution, didPluginsRun);
  }

  /**
   * Will determine from which plugin of the workflow to start execution from and will iterate
   * through the plugins of the workflow and run them one by one.
   * <p>It returns a {@link Pair} of a finished {@link Date} and a {@link Boolean} flag.
   * <ul>
   *   <li>
   *     The Date represents the finished date of the workflow or null if it did not finish
   *     as expected. That can happen if an error occurred or some plugin was not permitted to
   *     run.
   *   </li>
   *   <li>
   *     The Boolean flag represents true if all plugins were allowed to run or false if one of
   *     the plugins was not allowed to run.
   *   </li>
   * </ul>
   * </p>
   *
   * @return The pair of date and boolean flag
   */
  private Pair<Date, Boolean> runInqueueOrRunningStateWorkflowExecution() {

    // Find the first plugin to continue execution from
    int firstPluginPositionToStart = getFirstPluginPositionToStart();

    boolean didPluginRun = true;
    boolean continueNextPlugin = true;
    List<AbstractMetisPlugin> metisPlugins = workflowExecution.getMetisPlugins();
    // One by one start the plugins of the workflow
    for (int i = firstPluginPositionToStart;
        i < metisPlugins.size() && continueNextPlugin; i++) {
      final AbstractMetisPlugin plugin = metisPlugins.get(i);

      //Run plugin if available space
      didPluginRun = runMetisPluginWithSemaphoreAllocation(i, plugin);
      continueNextPlugin = !currentThread().isInterrupted() && didPluginRun && (
          !workflowExecutionDao.isCancelling(workflowExecution.getId())
              || plugin.getFinishedDate() != null)
          && plugin.getPluginStatus() != PluginStatus.FAILED;
    }

    // Compute the finished date
    final AbstractMetisPlugin lastPlugin = metisPlugins.get(metisPlugins.size() - 1);
    final Date finishDate;
    if (lastPlugin.getPluginStatus() == PluginStatus.FINISHED) {
      finishDate = lastPlugin.getFinishedDate();
    } else {
      finishDate = null;
    }
    return new ImmutablePair<>(finishDate, didPluginRun);
  }

  private int getFirstPluginPositionToStart() {
    int firstPluginPositionToStart = 0;
    List<AbstractMetisPlugin> metisPlugins = workflowExecution.getMetisPlugins();
    for (int i = 0; i < metisPlugins.size(); i++) {
      AbstractMetisPlugin metisPlugin = metisPlugins.get(i);
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING
          || metisPlugin.getPluginStatus() == PluginStatus.CLEANING
          || metisPlugin.getPluginStatus() == PluginStatus.PENDING
          || metisPlugin.getPluginStatus() == PluginStatus.IDENTIFYING_DELETED_RECORDS) {
        firstPluginPositionToStart = i;
        break;
      }
    }
    return firstPluginPositionToStart;
  }

  /**
   * Tries to acquire a semaphore permission corresponding to the provided plugin's type.
   * <ol>
   *   <li>If semaphore permission granted then there is space for that plugin and the plugin
   *   starts</li>
   *   <li>If semaphore permission NOT granted then the plugin din not run and a false flag is
   *   send back as a return result</li>
   * </ol>
   *
   * @param i the index of the plugin in the list of plugins inside the workflow execution
   * @param plugin the provided plugin to be ran
   * @return true if plugin ran, false if plugin did not run
   */
  private boolean runMetisPluginWithSemaphoreAllocation(int i, AbstractMetisPlugin plugin) {
    // Sanity check
    if (plugin == null) {
      throw new IllegalStateException("Plugin cannot be null.");
    }
    // Check the plugin: it has to be executable
    AbstractExecutablePlugin executablePlugin = expectExecutablePlugin(plugin);

    final ExecutablePluginType executablePluginType = ExecutablePluginType
        .getExecutablePluginFromPluginType(executablePlugin.getPluginType());
    if (executablePluginType == null) {
      throw new IllegalStateException("Plugin type cannot be null.");
    }

    //Try acquire semaphore and run plugin. Don't forget to release
    boolean acquired = semaphoresPerPluginManager
        .tryAcquireForExecutablePluginType(executablePluginType);
    if (acquired) {
      try {
        LOGGER.debug("workflowExecutionId: {}, executablePluginType: {} - Acquired semaphore",
            workflowExecution.getId(), executablePluginType);
        final Date startDateToUse = i == 0 ? workflowExecution.getStartedDate() : new Date();
        runMetisPlugin(executablePlugin, startDateToUse, workflowExecution.getDatasetId());
      } finally {
        semaphoresPerPluginManager.releaseForPluginType(executablePluginType);
        LOGGER.debug("workflowExecutionId: {}, executablePluginType: {} - Released semaphore",
            workflowExecution.getId(), executablePluginType);
      }
    } else {
      // Rest workflow execution to INQUEUE so that it can be reclaimed
      workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
      workflowExecutionDao.updateMonitorInformation(workflowExecution);
    }
    return acquired;
  }

  /**
   * It will prepare the plugin, request the external execution and will periodically monitor,
   * update the plugin's progress and at the end finalize the plugin's status and finished date.
   *
   * @param plugin the plugin to run
   * @param startDateToUse The date that should be used as start date (if the plugin is not already
   * running).
   * @param datasetId The dataset ID.
   */
  private void runMetisPlugin(AbstractExecutablePlugin<?> plugin, Date startDateToUse,
      String datasetId) {
    try {
      // Compute previous plugin revision information. Only need to look within the workflow: when
      // scheduling the workflow, the previous plugin information is set for the first plugin.
      final AbstractExecutablePluginMetadata metadata = plugin.getPluginMetadata();
      final ExecutedMetisPluginId executedMetisPluginId = ExecutedMetisPluginId
          .forPredecessor(plugin);
      if (executedMetisPluginId == null) {
        final ExecutablePlugin predecessor = DataEvolutionUtils
            .computePredecessorPlugin(metadata.getExecutablePluginType(), workflowExecution);
        if (predecessor != null) {
          metadata.setPreviousRevisionInformation(predecessor);
          // Save so that we can use it below to find the root ancestor.
          workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
        }
      }

      // Compute base harvesting plugin information. We can't do this when creating the workflow
      // execution: the harvest might be part of this very workflow.
      if (DataEvolutionUtils.getIndexPluginGroup()
              .contains(plugin.getPluginMetadata().getExecutablePluginType())) {
        final PluginWithExecutionId<ExecutablePlugin> rootAncestor = new DataEvolutionUtils(
                workflowExecutionDao).getRootAncestor(
                new PluginWithExecutionId<>(workflowExecution, plugin));
        setHarvestParametersToIndexingPlugin(plugin, rootAncestor.getPlugin());
      }

      // Start execution if it has not already started
      if (StringUtils.isEmpty(plugin.getExternalTaskId())) {
        if (plugin.getPluginStatus() == PluginStatus.INQUEUE) {
          plugin.setStartedDate(startDateToUse);
        }
        final DpsTaskSettings dpsTaskSettings = new DpsTaskSettings(
            ecloudBaseUrl, ecloudProvider, workflowExecution.getEcloudDatasetId(),
            getExternalTaskIdOfPreviousPlugin(metadata), metisCoreBaseUrl, throttlingValues);
        plugin
            .execute(workflowExecution.getDatasetId(), dpsClient, dpsTaskSettings);
      }
    } catch (ExternalTaskException | RuntimeException e) {
      LOGGER.warn(String.format("workflowExecutionId: %s, pluginType: %s - Execution of plugin "
          + "failed", workflowExecution.getId(), plugin.getPluginType()), e);
      plugin.setFinishedDate(null);
      plugin.setPluginStatusAndResetFailMessage(PluginStatus.FAILED);
      plugin.setFailMessage(String.format(DETAILED_EXCEPTION_FORMAT, TRIGGER_ERROR_PREFIX,
          ExceptionUtils.getStackTrace(e)));
      return;
    } finally {
      workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    }

    // Start periodical check and wait for plugin to be done
    long sleepTime = TimeUnit.SECONDS.toMillis(monitorCheckIntervalInSecs);
    periodicCheckingLoop(sleepTime, plugin, datasetId);
  }

  private void setHarvestParametersToIndexingPlugin(ExecutablePlugin indexingPlugin,
          ExecutablePlugin harvestPlugin) {

    // Check the harvesting types
    if (!DataEvolutionUtils.getHarvestPluginGroup()
            .contains(harvestPlugin.getPluginMetadata().getExecutablePluginType())) {
      throw new IllegalStateException(String.format(
              "workflowExecutionId: %s, pluginId: %s - Found plugin root that is not a harvesting plugin.",
              workflowExecution.getId(), indexingPlugin.getId()));
    }

    // get the information from the harvesting plugin.
    final boolean incrementalHarvest =
            (harvestPlugin.getPluginMetadata() instanceof AbstractHarvestPluginMetadata)
                    && ((AbstractHarvestPluginMetadata) harvestPlugin.getPluginMetadata())
                    .isIncrementalHarvest();
    final Date harvestDate = harvestPlugin.getStartedDate();

    // Set the information to the indexing plugin.
    if (indexingPlugin.getPluginMetadata() instanceof AbstractIndexPluginMetadata) {
      final var metadata = (AbstractIndexPluginMetadata) indexingPlugin.getPluginMetadata();
      metadata.setIncrementalIndexing(incrementalHarvest);
      metadata.setHarvestDate(harvestDate);
    }
  }

  private String getExternalTaskIdOfPreviousPlugin(AbstractExecutablePluginMetadata metadata) {

    // Get the previous plugin parameters from the plugin - if there is none, we are done.
    final ExecutedMetisPluginId predecessorPlugin = ExecutedMetisPluginId.forPredecessor(metadata);
    if (predecessorPlugin == null) {
      return null;
    }

    // Get the previous plugin based on the parameters.
    final WorkflowExecution previousExecution = workflowExecutionDao
        .getByTaskExecution(predecessorPlugin, workflowExecution.getDatasetId());
    return Optional.ofNullable(previousExecution)
        .flatMap(execution -> execution.getMetisPluginWithType(predecessorPlugin.getPluginType()))
        .map(this::expectExecutablePlugin).map(AbstractExecutablePlugin::getExternalTaskId)
        .orElse(null);
  }

  private AbstractExecutablePlugin expectExecutablePlugin(AbstractMetisPlugin plugin) {
    if (plugin == null || plugin instanceof AbstractExecutablePlugin) {
      return (AbstractExecutablePlugin) plugin;
    }
    throw new IllegalStateException(String.format(
        "workflowExecutionId: %s, pluginId: %s - Found plugin that is not an executable plugin.",
        workflowExecution.getId(), plugin.getId()));
  }

  private void periodicCheckingLoop(long sleepTime, AbstractExecutablePlugin plugin,
      String datasetId) {
    MonitorResult monitorResult = null;
    int consecutiveCancelOrMonitorFailures = 0;
    AtomicBoolean externalCancelCallSent = new AtomicBoolean(false);
    AtomicInteger previousProcessedRecords = new AtomicInteger(0);
    AtomicLong checkPointDateOfProcessedRecordsPeriodInMillis = new AtomicLong(
        System.currentTimeMillis());
    do {
      try {
        Thread.sleep(sleepTime);
        // Check if the task is cancelling and send the external cancelling call if needed
        sendExternalCancelCallIfNeeded(externalCancelCallSent, plugin, previousProcessedRecords,
            checkPointDateOfProcessedRecordsPeriodInMillis);
        monitorResult = plugin.monitor(dpsClient);
        consecutiveCancelOrMonitorFailures = 0;

        if (monitorResult.getTaskState() == TaskState.REMOVING_FROM_SOLR_AND_MONGO ||
            isIndexingInPostProcessing(monitorResult, plugin)) {
          plugin.setPluginStatusAndResetFailMessage(PluginStatus.CLEANING);

        } else if (isHarvestingInPostProcessing(monitorResult, plugin)) {
          plugin.setPluginStatusAndResetFailMessage(PluginStatus.IDENTIFYING_DELETED_RECORDS);

        } else {
          plugin.setPluginStatusAndResetFailMessage(PluginStatus.RUNNING);
        }

      } catch (InterruptedException e) {
        LOGGER.warn(String.format(
            "workflowExecutionId: %s, pluginType: %s - Thread was interrupted during monitoring of external task",
            workflowExecution.getId(), plugin.getPluginType()), e);
        currentThread().interrupt();
        return;
      } catch (ExternalTaskException e) {
        final Throwable cause = ExternalRequestUtil.getRootCause(e);
        // TODO: 14/05/2021 We might want to remove this or find a better way to handle the shutdown of metis-core because, it seems that this occurred during an execution of a plugin and therefore the plugin went out of the loop.
        // The next plugin was tried but was marked FAILED because the previous plugin didn't finish and it automatically marked the previous plugin CANCELLED
        // which shouldn't happen.(The plugin was still in progress in ecloud)
//        if (cause instanceof IllegalStateException) {
//          //If the application has a forcible shutdown we might experience the JerseyClient in DpsClient
//          // internally closing down without our consent even if we would have a synchronized close
//          // implemented. This catch gives us a little more assurance of avoiding an execution
//          // being marked as failed.
//          LOGGER.warn("Application is probably shutting down at the moment and dpsClient has "
//              + "closed without our consent, so we are ignoring this exception.", e);
//          return;
//        }
        LOGGER.warn(String
            .format("workflowExecutionId: %s, pluginType: %s - ExternalTaskException occurred.",
                workflowExecution.getId(), plugin.getPluginType()), e);
        // TODO: 08/01/2021 Remove the check on MessageBodyProviderNotFoundException when
        //  DpsClient is updated and doesn't throw it anymore
        if (!ExternalRequestUtil.doesExceptionCauseMatchAnyOfProvidedExceptions(
            UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, e)
            && !(cause instanceof MessageBodyProviderNotFoundException)
            && !(cause instanceof IllegalStateException)) {
          // Set plugin to FAILED and return immediately
          plugin.setFinishedDate(null);
          plugin.setPluginStatusAndResetFailMessage(PluginStatus.FAILED);
          plugin.setFailMessage(String.format(DETAILED_EXCEPTION_FORMAT, MONITOR_ERROR_PREFIX,
              ExceptionUtils.getStackTrace(e)));
          return;
        }
        consecutiveCancelOrMonitorFailures++;
        LOGGER.warn(String.format(
            "workflowExecutionId: %s, pluginType: %s - Monitoring of external task failed %s "
                + "consecutive times. After exceeding %s retries, pending status will be set",
            workflowExecution.getId(), plugin.getPluginType(), consecutiveCancelOrMonitorFailures,
            MAX_CANCEL_OR_MONITOR_FAILURES), e);
        if (consecutiveCancelOrMonitorFailures >= MAX_CANCEL_OR_MONITOR_FAILURES) {
          plugin.setPluginStatusAndResetFailMessage(PluginStatus.PENDING);
        }
      } finally {
        Date updatedDate = new Date();
        plugin.setUpdatedDate(updatedDate);
        workflowExecution.setUpdatedDate(updatedDate);
        workflowExecutionDao.updateMonitorInformation(workflowExecution);
      }
    } while (isContinueMonitor(monitorResult));

    // Perform post-processing if needed.
    if (!applyPostProcessing(monitorResult, plugin, datasetId)) {
      return;
    }

    // Set the status of the task.
    preparePluginStateAndFinishedDate(plugin, monitorResult);
  }

  private boolean isIndexingInPostProcessing(MonitorResult monitor,
      AbstractExecutablePlugin plugin) {
    return monitor.getTaskState() == TaskState.IN_POST_PROCESSING &&
        (plugin.getPluginType() == PluginType.REINDEX_TO_PREVIEW ||
            plugin.getPluginType() == PluginType.REINDEX_TO_PUBLISH);
  }

  private boolean isHarvestingInPostProcessing(MonitorResult monitor, AbstractExecutablePlugin plugin) {
    return monitor.getTaskState() == TaskState.IN_POST_PROCESSING &&
        (plugin.getPluginType() == PluginType.HTTP_HARVEST ||
            plugin.getPluginType() == PluginType.OAIPMH_HARVEST);
  }

  private void sendExternalCancelCallIfNeeded(AtomicBoolean externalCancelCallSent,
      AbstractExecutablePlugin plugin, AtomicInteger previousProcessedRecords,
      AtomicLong checkPointDateOfProcessedRecordsPeriodInMillis) throws ExternalTaskException {
    if (!externalCancelCallSent.get() && shouldPluginBeCancelled(plugin, previousProcessedRecords,
        checkPointDateOfProcessedRecordsPeriodInMillis)) {
      // Update workflowExecution first, to retrieve cancelling information from db
      workflowExecution = workflowExecutionDao.getById(workflowExecution.getId().toString());
      plugin.cancel(dpsClient, workflowExecution.getCancelledBy());
      externalCancelCallSent.set(true);
    }
  }

  private boolean applyPostProcessing(MonitorResult monitorResult, AbstractExecutablePlugin plugin,
      String datasetId) {
    boolean processingAppliedOrNotRequired = true;
    if (monitorResult.getTaskState() == TaskState.PROCESSED) {
      try {
        this.workflowPostProcessor.performPluginPostProcessing(plugin, datasetId);
      } catch (DpsException | InvalidIndexPluginException | BadContentException | RuntimeException e) {
        processingAppliedOrNotRequired = false;
        LOGGER.warn("Problem occurred during Metis post-processing.", e);
        plugin.setFinishedDate(null);
        plugin.setPluginStatusAndResetFailMessage(PluginStatus.FAILED);
        plugin.setFailMessage(String.format(DETAILED_EXCEPTION_FORMAT, POSTPROCESS_ERROR_PREFIX,
            ExceptionUtils.getStackTrace(e)));
      }
    }
    return processingAppliedOrNotRequired;
  }

  private boolean isContinueMonitor(MonitorResult monitorResult) {
    return monitorResult == null || (monitorResult.getTaskState() != TaskState.DROPPED
        && monitorResult.getTaskState() != TaskState.PROCESSED);
  }

  private boolean shouldPluginBeCancelled(AbstractExecutablePlugin plugin,
      AtomicInteger previousProcessedRecords,
      AtomicLong checkPointDateOfProcessedRecordsPeriodInMillis) {
    // A plugin with CLEANING state is NOT cancellable, it will be when the state is updated
    final boolean notCleaningAndCancelling =
        plugin.getPluginStatus() != PluginStatus.CLEANING && workflowExecutionDao
            .isCancelling(workflowExecution.getId());
    // A cleaning or a pending task should not be cancelled by exceeding the minute cap
    final boolean notCleaningOrPending = plugin.getPluginStatus() != PluginStatus.CLEANING
        && plugin.getPluginStatus() != PluginStatus.PENDING;
    final boolean isMinuteCapExceeded = isMinuteCapOverWithoutChangeInProcessedRecords(plugin,
        previousProcessedRecords, checkPointDateOfProcessedRecordsPeriodInMillis);
    return (notCleaningAndCancelling || (notCleaningOrPending && isMinuteCapExceeded));
  }

  private boolean isMinuteCapOverWithoutChangeInProcessedRecords(AbstractExecutablePlugin<?> plugin,
      AtomicInteger previousProcessedRecords,
      AtomicLong checkPointDateOfProcessedRecordsPeriodInMillis) {
    final int processedRecords = plugin.getExecutionProgress().getProcessedRecords();
    //If CLEANING is in progress then just reset the values to be sure and return false
    //Or if we have progress
    if (plugin.getPluginStatus() == PluginStatus.CLEANING
        || plugin.getPluginStatus() == PluginStatus.PENDING
        || previousProcessedRecords.get() != processedRecords) {
      checkPointDateOfProcessedRecordsPeriodInMillis.set(System.currentTimeMillis());
      previousProcessedRecords.set(processedRecords);
      return false;
    }

    final boolean isMinuteCapOverWithoutChangeInProcessedRecords = TimeUnit.MILLISECONDS.toSeconds(
        System.currentTimeMillis() - checkPointDateOfProcessedRecordsPeriodInMillis.get())
        >= periodOfNoProcessedRecordsChangeInSeconds;
    if (isMinuteCapOverWithoutChangeInProcessedRecords) {
      //Request cancelling of the execution
      workflowExecutionDao.setCancellingState(workflowExecution, null);
    }
    return isMinuteCapOverWithoutChangeInProcessedRecords;
  }

  private void preparePluginStateAndFinishedDate(AbstractExecutablePlugin<?> plugin,
      MonitorResult monitorResult) {
    if (monitorResult.getTaskState() == TaskState.PROCESSED) {
      plugin.setFinishedDate(new Date());
      plugin.setPluginStatusAndResetFailMessage(PluginStatus.FINISHED);
    } else if (monitorResult.getTaskState() == TaskState.DROPPED && !workflowExecutionDao
        .isCancelling(workflowExecution.getId())) {
      plugin.setPluginStatusAndResetFailMessage(PluginStatus.FAILED);
      final String failMessage =
          StringUtils.isBlank(monitorResult.getTaskInfo()) ? "No further information received."
              : monitorResult.getTaskInfo();
      plugin.setFailMessage(EXECUTION_ERROR_PREFIX + failMessage);
    }
    workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
  }
}
