package eu.europeana.metis.core.execution;

import static eu.europeana.metis.network.ExternalRequestUtil.UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS;
import static java.lang.Thread.currentThread;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dao.WorkflowUtils;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.EcloudBasePluginParameters;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin.MonitorResult;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.network.ExternalRequestUtil;
import java.util.Date;
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

  private static final String EXECUTION_ERROR_PREFIX = "Execution of external task presented with an error. ";
  private static final String MONITOR_ERROR_PREFIX = "An error occurred while monitoring the external task. ";
  private static final String POSTPROCESS_ERROR_PREFIX = "An error occurred while post-processing the external task. ";
  private static final String TRIGGER_ERROR_PREFIX = "An error occurred while triggering the external task. ";
  private static final String DETAILED_EXCEPTION_FORMAT = "%s%nDetailed exception:%s";

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutor.class);
  private static final int MAX_CANCEL_OR_MONITOR_FAILURES = 3;

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
        LOGGER
            .info("workflowExecution: {} - Stop workflow execution a plugin was not allowed to run",
                workflowExecution.getId());
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
    // One by one start the plugins of the workflow
    for (int i = firstPluginPositionToStart;
        i < workflowExecution.getMetisPlugins().size() && continueNextPlugin; i++) {
      final AbstractMetisPlugin plugin = workflowExecution.getMetisPlugins().get(i);

      //Run plugin if available space
      didPluginRun = runMetisPluginWithSemaphoreAllocation(i, plugin);
      continueNextPlugin = !currentThread().isInterrupted() && didPluginRun && (
          !workflowExecutionDao.isCancelling(workflowExecution.getId())
              || plugin.getFinishedDate() != null)
          && plugin.getPluginStatus() != PluginStatus.FAILED;
    }

    // Compute the finished date
    final AbstractMetisPlugin lastPlugin = workflowExecution.getMetisPlugins()
        .get(workflowExecution.getMetisPlugins().size() - 1);
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
    for (int i = 0; i < workflowExecution.getMetisPlugins().size(); i++) {
      AbstractMetisPlugin metisPlugin = workflowExecution.getMetisPlugins().get(i);
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING
          || metisPlugin.getPluginStatus() == PluginStatus.CLEANING
          || metisPlugin.getPluginStatus() == PluginStatus.PENDING) {
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
   * @throws InterruptedException if an interruption occurred
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
   * @param executablePlugin the plugin to run
   * @param startDateToUse The date that should be used as start date (if the plugin is not already
   * running).
   * @param datasetId The dataset ID.
   */
  private void runMetisPlugin(AbstractExecutablePlugin<?> executablePlugin, Date startDateToUse,
      String datasetId) {
    try {
      // Compute previous plugin revision information. Only need to look within the workflow: when
      // scheduling the workflow, the previous plugin information is set for the first plugin.
      final AbstractExecutablePluginMetadata metadata = executablePlugin.getPluginMetadata();
      if (metadata.getRevisionTimestampPreviousPlugin() == null
          || metadata.getRevisionNamePreviousPlugin() == null) {
        final AbstractExecutablePlugin predecessor = WorkflowUtils
            .computePredecessorPlugin(metadata.getExecutablePluginType(), workflowExecution);
        if (predecessor != null) {
          metadata.setPreviousRevisionInformation(predecessor);
        }
      }

      // Start execution if it has not already started
      if (StringUtils.isEmpty(executablePlugin.getExternalTaskId())) {
        if (executablePlugin.getPluginStatus() == PluginStatus.INQUEUE) {
          executablePlugin.setStartedDate(startDateToUse);
        }
        final EcloudBasePluginParameters ecloudBasePluginParameters = new EcloudBasePluginParameters(
            ecloudBaseUrl, ecloudProvider, workflowExecution.getEcloudDatasetId(),
            getExternalTaskIdOfPreviousPlugin(metadata), metisCoreBaseUrl);
        executablePlugin
            .execute(workflowExecution.getDatasetId(), dpsClient, ecloudBasePluginParameters);
      }
    } catch (ExternalTaskException | RuntimeException e) {
      LOGGER.warn(String
          .format("workflowExecutionId: %s, pluginType: %s - Execution of plugin " + "failed",
              workflowExecution.getId(), executablePlugin.getPluginType()), e);
      executablePlugin.setFinishedDate(null);
      executablePlugin.setPluginStatusAndResetFailMessage(PluginStatus.FAILED);
      executablePlugin.setFailMessage(String.format(DETAILED_EXCEPTION_FORMAT, TRIGGER_ERROR_PREFIX,
          ExceptionUtils.getStackTrace(e)));
      return;
    } finally {
      workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    }

    // Start periodical check and wait for plugin to be done
    long sleepTime = TimeUnit.SECONDS.toMillis(monitorCheckIntervalInSecs);
    periodicCheckingLoop(sleepTime, executablePlugin, datasetId);
  }

  private String getExternalTaskIdOfPreviousPlugin(
      AbstractExecutablePluginMetadata pluginMetadata) {

    // Get the previous plugin parameters from the plugin - if there is none, we are done.
    final PluginType previousPluginType = PluginType
        .getPluginTypeFromEnumName(pluginMetadata.getRevisionNamePreviousPlugin());
    final Date previousPluginStartDate = pluginMetadata.getRevisionTimestampPreviousPlugin();
    if (previousPluginType == null || previousPluginStartDate == null) {
      return null;
    }

    // Get the previous plugin based on the parameters.
    final WorkflowExecution previousExecution = workflowExecutionDao
        .getByTaskExecution(previousPluginStartDate, previousPluginType,
            workflowExecution.getDatasetId());
    return Optional.ofNullable(previousExecution)
        .flatMap(execution -> execution.getMetisPluginWithType(previousPluginType))
        .map(WorkflowExecutor::expectExecutablePlugin)
        .map(AbstractExecutablePlugin::getExternalTaskId).orElse(null);
  }

  private static AbstractExecutablePlugin expectExecutablePlugin(AbstractMetisPlugin plugin) {
    if (plugin == null || plugin instanceof AbstractExecutablePlugin) {
      return (AbstractExecutablePlugin) plugin;
    }
    throw new IllegalStateException("Workflow executor found plugin with ID " + plugin.getId()
        + " that is not an executable plugin.");
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
        plugin.setPluginStatusAndResetFailMessage(
            monitorResult.getTaskState() == TaskState.REMOVING_FROM_SOLR_AND_MONGO
                ? PluginStatus.CLEANING : PluginStatus.RUNNING);
      } catch (InterruptedException e) {
        LOGGER.warn(String.format(
            "workflowExecutionId: %s, pluginType: %s - Thread was interrupted during monitoring of external task",
            workflowExecution.getId(), plugin.getPluginType()), e);
        currentThread().interrupt();
        return;
      } catch (ExternalTaskException e) {
        LOGGER.warn(String
            .format("workflowExecutionId: %s, pluginType: %s - ExternalTaskException occurred.",
                workflowExecution.getId(), plugin.getPluginType()), e);
        if (!ExternalRequestUtil.doesExceptionCauseMatchAnyOfProvidedExceptions(
            UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, e)) {
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
        if (consecutiveCancelOrMonitorFailures == MAX_CANCEL_OR_MONITOR_FAILURES) {
          //Set pending status once
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
      } catch (DpsException | RuntimeException e) {
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
