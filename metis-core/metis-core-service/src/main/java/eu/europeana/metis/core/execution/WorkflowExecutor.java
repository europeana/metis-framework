package eu.europeana.metis.core.execution;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.exception.ExternalTaskException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a {@link Callable} class that accepts a {@link WorkflowExecution}.
 * It starts that WorkflowExecution given to it and will continue monitoring and updating its
 * progress until it ends either by user interaction or by the end of the Workflow.
 * When the WorkflowExecution is received there is a chance that the execution is already being handled
 * from another WorkflowExecutor in another instance and if that is the case the WorkflowExecution will
 * be dropped.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class WorkflowExecutor implements Callable<WorkflowExecution> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutor.class);
  private static final String EXECUTION_CHECK_LOCK = "EXECUTION_CHECK_LOCK";
  private static final int MONITOR_ITERATIONS_TO_FAKE = 2;
  private static final int FAKE_RECORDS_PER_ITERATION = 100;
  private static final int MAX_CANCEL_OR_MONITOR_FAILURES = 3;
  private Date finishDate;
  private boolean firstPluginExecution;
  private final int monitorCheckIntervalInSecs;

  private final WorkflowExecution workflowExecution;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final RedissonClient redissonClient;
  private final DpsClient dpsClient;
  private final String ecloudBaseUrl;
  private final String ecloudProvider;

  WorkflowExecutor(WorkflowExecution workflowExecution,
      WorkflowExecutionDao workflowExecutionDao, int monitorCheckIntervalInSecs,
      RedissonClient redissonClient, DpsClient dpsClient, String ecloudBaseUrl,
      String ecloudProvider) {
    this.workflowExecution = workflowExecution;
    this.workflowExecutionDao = workflowExecutionDao;
    this.monitorCheckIntervalInSecs = monitorCheckIntervalInSecs;
    this.redissonClient = redissonClient;
    this.dpsClient = dpsClient;
    this.ecloudBaseUrl = ecloudBaseUrl;
    this.ecloudProvider = ecloudProvider;
  }

  @Override
  public WorkflowExecution call() {
    //Get lock for the case that two cores will retrieve the same execution id from 2 items in the queue in different cores
    RLock lock = redissonClient.getFairLock(EXECUTION_CHECK_LOCK);
    try {
      lock.lock();
      LOGGER.info("Starting user workflow execution with id: {} and priority {}",
          workflowExecution.getId(), workflowExecution.getWorkflowPriority());
      firstPluginExecution = true;
      if ((workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE
          || workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING)
          && !workflowExecutionDao
          .isExecutionActive(this.workflowExecution, monitorCheckIntervalInSecs)) {
        prepareWorkflowForStartingExecution();
        lock.unlock(); //Unlock as soon as possible
        runInqueueOrRunningStateWorkflowExecution();
      } else {
        LOGGER.info(
            "Discarding WorkflowExecution with id: {}, it is either currently handled from another instance or it not INQUEUE or RUNNING",
            workflowExecution.getId());
        return workflowExecution;
      }
    } finally {
      lock.unlock();
    }

    //Cancel workflow and all other than finished plugins if the workflow was cancelled during execution
    //Check if there is no finished date to make sure the workflow hasn't actually already finished
    if (workflowExecutionDao.isCancelling(workflowExecution.getId()) && finishDate == null) {
      workflowExecution.setAllRunningAndInqueuePluginsToCancelled();
      LOGGER.info(
          "Cancelled running user workflow execution with id: {}", workflowExecution.getId());
    } else if (finishDate != null) { //finishedDate can be null in case of Exception
      workflowExecution.setFinishedDate(finishDate);
      workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      workflowExecution.setCancelling(false);
      LOGGER.info("Finished user workflow execution with id: {}", workflowExecution.getId());
    } else { //In case of failure
      workflowExecution.checkAndSetAllRunningAndInqueuePluginsToCancelledIfOnePluginHasFailed();
    }
    //The only full update is used here. The rest of the execution uses partial updates to avoid losing the cancelling state field
    workflowExecutionDao.update(workflowExecution);
    return workflowExecution;
  }

  private void prepareWorkflowForStartingExecution() {
    //Run if the workflowExecution was retrieved from the queue in RUNNING state. Another process released it and came back into the queue
    if (workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING) {
      workflowExecution.setUpdatedDate(new Date());
    } else {
      workflowExecution.setStartedDate(new Date());
    }
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecutionDao.updateMonitorInformation(workflowExecution);
  }

  /**
   * Will determine from which plugin of the workflow to start execution from and will iterate
   * through the plugins of the workflow one by one.
   */
  private void runInqueueOrRunningStateWorkflowExecution() {
    int firstPluginPositionToStart = 0;
    //Find the first plugin to continue execution from
    for (int i = 0; i < workflowExecution.getMetisPlugins().size(); i++) {
      AbstractMetisPlugin metisPlugin = workflowExecution.getMetisPlugins().get(i);
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING) {
        firstPluginPositionToStart = i;
        break;
      }
    }
    if (firstPluginPositionToStart != 0
        || workflowExecution.getMetisPlugins().get(0).getPluginStatus() == PluginStatus.RUNNING) {
      firstPluginExecution = false; //Used to set the proper startedDate on the first plugin that will be executed in the workflow
    }
    //One by one start the plugins of the workflow
    AbstractMetisPlugin previousMetisPlugin = null;
    for (int i = firstPluginPositionToStart; i < workflowExecution.getMetisPlugins().size(); i++) {
      AbstractMetisPlugin metisPlugin = workflowExecution.getMetisPlugins().get(i);
      finishDate = runMetisPlugin(previousMetisPlugin, metisPlugin);
      if ((workflowExecutionDao.isCancelling(workflowExecution.getId()) && finishDate == null)
          || metisPlugin.getPluginStatus() == PluginStatus.FAILED) {
        break;
      }
      previousMetisPlugin = metisPlugin;
    }
  }

  /**
   * It will prepare the plugin, request the external execution and will periodically monitor, update the plugin's progress
   * and at the end finalize the plugin's status and finished date.
   *
   * @param previousAbstractMetisPlugin the plugin that was ran before the current plugin if any
   * @param abstractMetisPlugin the current plugin to run
   * @return the finished date of the last plugin or null if there was an exception or the workflow was CANCELLED
   */
  private Date runMetisPlugin(AbstractMetisPlugin previousAbstractMetisPlugin,
      AbstractMetisPlugin abstractMetisPlugin) {


    if (previousAbstractMetisPlugin != null) { //Get previous plugin revision information
      abstractMetisPlugin.getPluginMetadata().setPreviousRevisionInformation(previousAbstractMetisPlugin);
    }

    //Start execution and periodical check
    if (StringUtils.isEmpty(abstractMetisPlugin.getExternalTaskId())) {
      try {
        //Determine the startedDate for the plugin
        if (abstractMetisPlugin.getPluginStatus() == PluginStatus.INQUEUE) {
          if (firstPluginExecution) {
            firstPluginExecution = false;
            abstractMetisPlugin.setStartedDate(workflowExecution.getStartedDate());
          } else {
            abstractMetisPlugin.setStartedDate(new Date());
          }
        }
        abstractMetisPlugin
            .execute(dpsClient, ecloudBaseUrl, ecloudProvider,
                workflowExecution.getEcloudDatasetId());
        abstractMetisPlugin.setPluginStatus(PluginStatus.RUNNING);
        workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
      } catch (ExternalTaskException e) {
        LOGGER.warn("Execution of external task failed", e);
        abstractMetisPlugin.setFinishedDate(null);
        abstractMetisPlugin.setPluginStatus(PluginStatus.FAILED);
        workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
        return abstractMetisPlugin.getFinishedDate();
      }
    }

    long sleepTime = TimeUnit.SECONDS.toMillis(monitorCheckIntervalInSecs);
    if (!abstractMetisPlugin.getPluginMetadata().isMocked()) {
      return periodicCheckingLoop(sleepTime, abstractMetisPlugin);
    } else {
      return periodicCheckingLoopMocked(sleepTime, MONITOR_ITERATIONS_TO_FAKE, abstractMetisPlugin);
    }
  }

  private Date periodicCheckingLoop(long sleepTime, AbstractMetisPlugin abstractMetisPlugin) {
    TaskState taskState = null;
    int consecutiveCancelOrMonitorFailures = 0;
    boolean externalCancelCallSent = false;
    do {
      try {
        Thread.sleep(sleepTime);
        if (workflowExecutionDao.isCancelling(workflowExecution.getId())
            && !externalCancelCallSent) {
          abstractMetisPlugin.cancel(dpsClient);
          externalCancelCallSent = true;
        }
        taskState = abstractMetisPlugin.monitor(dpsClient).getStatus();
        consecutiveCancelOrMonitorFailures = 0;
        Date updatedDate = new Date();
        abstractMetisPlugin.setUpdatedDate(updatedDate);
        workflowExecution.setUpdatedDate(updatedDate);
        workflowExecutionDao.updateMonitorInformation(workflowExecution);
      } catch (InterruptedException e) {
        LOGGER.warn("Thread was interrupted", e);
        Thread.currentThread().interrupt();
        return null;
      } catch (ExternalTaskException e) {
        consecutiveCancelOrMonitorFailures++;
        LOGGER.warn(
            String.format("Monitoring of external task failed %s/%s",
                consecutiveCancelOrMonitorFailures,
                MAX_CANCEL_OR_MONITOR_FAILURES), e);
        if (consecutiveCancelOrMonitorFailures == MAX_CANCEL_OR_MONITOR_FAILURES) {
          break;
        }
      }
    } while (taskState == null || (taskState != TaskState.DROPPED
        && taskState != TaskState.PROCESSED));

    return preparePluginStateAndFinishedDate(abstractMetisPlugin, taskState,
        consecutiveCancelOrMonitorFailures);
  }

  private Date preparePluginStateAndFinishedDate(AbstractMetisPlugin abstractMetisPlugin,
      TaskState taskState,
      int consecutiveCancelOrMonitorFailures) {
    if (taskState == TaskState.PROCESSED) {
      abstractMetisPlugin.setFinishedDate(new Date());
      abstractMetisPlugin.setPluginStatus(PluginStatus.FINISHED);
    } else if ((taskState == TaskState.DROPPED && !workflowExecutionDao
        .isCancelling(workflowExecution.getId()))
        || consecutiveCancelOrMonitorFailures == MAX_CANCEL_OR_MONITOR_FAILURES) {
      abstractMetisPlugin.setPluginStatus(PluginStatus.FAILED);
    }

    workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    return abstractMetisPlugin.getFinishedDate();
  }

  private Date periodicCheckingLoopMocked(long sleepTime, int iterationsToFake,
      AbstractMetisPlugin abstractMetisPlugin) {
    for (int i = 1; i <= iterationsToFake; i++) {
      try {
        if (workflowExecutionDao.isCancelling(workflowExecution.getId())) {
          return null;
        }
        Thread.sleep(sleepTime);
        fakeMonitorUpdateProcessedRecords(abstractMetisPlugin, i, iterationsToFake,
            FAKE_RECORDS_PER_ITERATION);
        Date updatedDate = new Date();
        abstractMetisPlugin.setUpdatedDate(updatedDate);
        workflowExecution.setUpdatedDate(updatedDate);
        workflowExecutionDao.updateMonitorInformation(workflowExecution);
      } catch (InterruptedException e) {
        LOGGER.warn("Thread was interrupted", e);
        Thread.currentThread().interrupt();
        return null;
      }
    }
    abstractMetisPlugin.setFinishedDate(new Date());
    abstractMetisPlugin.setPluginStatus(PluginStatus.FINISHED);
    workflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    return abstractMetisPlugin.getFinishedDate();
  }

  private void fakeMonitorUpdateProcessedRecords(
      AbstractMetisPlugin abstractMetisPlugin, int iteration, int totalIterations,
      int recordPerIteration) {
    ExecutionProgress executionProgress = abstractMetisPlugin.getExecutionProgress();
    executionProgress.setExpectedRecords(totalIterations * recordPerIteration);
    executionProgress.setProcessedRecords(iteration * recordPerIteration);
    abstractMetisPlugin.setExecutionProgress(executionProgress);
  }
}
