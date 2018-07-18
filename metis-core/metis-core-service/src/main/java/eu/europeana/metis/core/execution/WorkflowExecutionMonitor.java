package eu.europeana.metis.core.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;

/**
 * This class monitors workflow executions. It provides functionality that determines whether a
 * running execution is progressing (as opposed to hanging) as well as functionality to deal with
 * this eventuality. Two of its methods are meant to be scheduled for a periodical run.
 */
public class WorkflowExecutionMonitor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutionMonitor.class);

  private static final String FAILSAFE_LOCK = "failsafeLock";

  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowExecutorManager workflowExecutorManager;
  private final Duration failsafeLeniency;
  private final RLock lock;

  /** The currently running executions. **/
  private Map<String, WorkflowExecutionEntry> currentRunningExecutions = Collections.emptyMap();

  /**
   * Constructor the executor
   *
   * @param workflowExecutorManager {@link WorkflowExecutorManager}
   * @param workflowExecutionDao {@link WorkflowExecutionDao}
   * @param redissonClient {@link RedissonClient}
   * @param failsafeLeniency The leniency given to executions to be idle.
   */
  public WorkflowExecutionMonitor(WorkflowExecutorManager workflowExecutorManager,
      WorkflowExecutionDao workflowExecutionDao, RedissonClient redissonClient,
      Duration failsafeLeniency) {
    this.failsafeLeniency = failsafeLeniency;
    this.workflowExecutionDao = workflowExecutionDao;
    this.workflowExecutorManager = workflowExecutorManager;
    this.lock = redissonClient.getFairLock(FAILSAFE_LOCK);
  }

  /* DO NOT CALL THIS METHOD WITHOUT POSSESSING THE LOCK */
  List<WorkflowExecution> updateCurrentRunningExecutions() {

    // Get all workflow executions that are currently running
    final List<WorkflowExecution> allRunningWorkflowExecutions =
        getWorkflowExecutionsWithStatus(WorkflowStatus.RUNNING);

    // Go by all running executions and compare them with the data we already have.
    final Map<String, WorkflowExecutionEntry> newExecutions = new HashMap<>();
    for (WorkflowExecution execution : allRunningWorkflowExecutions) {
      final WorkflowExecutionEntry currentEntry = getEntry(execution);
      final WorkflowExecutionEntry newEntry;
      if (currentEntry != null && currentEntry.updateTimeValueIsEqual(execution.getUpdatedDate())) {
        // If the known update time has not changed, we keep the entry (the
        // timeOfLastUpdateTimeChange property should not change).
        newEntry = currentEntry;
      } else {
        // If we find a change of the known update time, we make a new entry with a new
        // timeOfLastUpdateTimeChange value).
        newEntry = new WorkflowExecutionEntry(execution.getUpdatedDate());
      }
      newExecutions.put(execution.getId().toString(), newEntry);
    }
    currentRunningExecutions = Collections.unmodifiableMap(newExecutions);

    // Done: return all currently running executions.
    return allRunningWorkflowExecutions;
  }

  /**
   * Makes a run to check if there are running executions hanging and if some are found it will
   * re-send them in the distributed queue. To be safe (in case of the queue crashing) we also send
   * all executions that are marked as being in the queue to the queue again. This method is meant
   * to run periodically.
   */
  public void performFailsafe() {
    try {

      // Lock for the duration of this scheduled task
      lock.lock();

      // Update the execution times. This way we always have the latest values.
      final List<WorkflowExecution> allRunningWorkflowExecutions = updateCurrentRunningExecutions();

      // Determine which running executions appear to be hanging. Those we requeue. If an execution
      // is running but there is no entry, requeue it just to be safe (this can't happen).
      final List<WorkflowExecution> toBeRequeued = new ArrayList<>();
      for (WorkflowExecution runningExecution : allRunningWorkflowExecutions) {
        final WorkflowExecutionEntry executionEntry = getEntry(runningExecution);
        if (executionEntry == null || executionEntry.assumeHanging(failsafeLeniency)) {
          toBeRequeued.add(runningExecution);
        }
      }

      // Get all workflow executions that are currently in queue - they are all to be requeued.
      toBeRequeued.addAll(getWorkflowExecutionsWithStatus(WorkflowStatus.INQUEUE));

      // Requeue executions.
      for (WorkflowExecution workflowExecution : toBeRequeued) {
        workflowExecutorManager.addWorkflowExecutionToQueue(workflowExecution.getId().toString(),
            workflowExecution.getWorkflowPriority());
      }
    } catch (RuntimeException e) {
      LOGGER.warn(
          "Exception thrown from rabbitmq channel or Redis disconnection, failsafe thread continues",
          e);
    } finally {
      try {
        lock.unlock();
      } catch (RedisConnectionException e) {
        LOGGER.warn("Cannot connect to unlock, failsafe thread continues", e);
      }
    }
  }

  /* DO NOT CALL THIS METHOD WITHOUT POSSESSING THE LOCK */
  List<WorkflowExecution> getWorkflowExecutionsWithStatus(WorkflowStatus workflowStatus) {

    // Get all the executions, using paging.
    final List<WorkflowExecution> workflowExecutions = new ArrayList<>();
    int nextPage = 0;
    ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper =
        new ResponseListWrapper<>();
    do {
      userWorkflowExecutionResponseListWrapper.clear();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(
          workflowExecutionDao.getAllWorkflowExecutions(null, EnumSet.of(workflowStatus),
              OrderField.ID, true, nextPage),
          workflowExecutionDao.getWorkflowExecutionsPerRequest(), nextPage);
      workflowExecutions.addAll(userWorkflowExecutionResponseListWrapper.getResults());
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    // Done.
    return workflowExecutions;
  }

  /**
   * This method determines whether a workflow execution may be started. Executions in queue may
   * always be started. Running executions are granted or denied permission according to the
   * following rules:
   * <ol>
   * <li>If we know a last change time and it is recent, we assume that a process is already working
   * on this permission and permission is denied.</li>
   * <li>If we know a last change time and it is old, but the database has a different update time,
   * permission is denied as this signifies a change (which will be captured by the next run of the
   * monitor).</li>
   * <li>If we don't have a monitor last change time, we assume that this will appear shortly and we
   * postpone the decision (by denying it now). This shouldn't happen.</li>
   * <li>In all other cases permission is granted: the execution is determined to be hanging.</li>
   * </ol>
   * 
   * @param workflowExecutionId The ID of the workflow execution which the caller wishes to claim.
   * @return A recent version of the workflow execution if the claim is granted. Null if the claim
   *         is denied.
   */
  public WorkflowExecution claimExecution(String workflowExecutionId) {

    try {

      // Lock for the duration of this request
      lock.lock();

      // Retrieve the most current version of the execution.
      final WorkflowExecution workflowExecution = workflowExecutionDao.getById(workflowExecutionId);

      // If we can't claim the execution, we're done.
      if (!mayClaimExecution(workflowExecution)) {
        return null;
      }

      // Otherwise prepare the execution for running.
      final Date now = new Date();
      workflowExecution.setUpdatedDate(now);
      if (workflowExecution.getWorkflowStatus() != WorkflowStatus.RUNNING) {
        workflowExecution.setStartedDate(now);
        workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
      }
      workflowExecutionDao.updateMonitorInformation(workflowExecution);

      // Done
      return workflowExecution;

    } catch (RuntimeException e) {
      LOGGER.warn("Exception thrown while claiming workflow execution.", e);
      return null;
    } finally {
      lock.unlock();
    }
  }

  /* DO NOT CALL THIS METHOD WITHOUT POSSESSING THE LOCK */
  boolean mayClaimExecution(WorkflowExecution workflowExecution) {

    // If the status is not RUNNING, we can give the answer straight away: only executions in the
    // queue may be started.
    if (workflowExecution.getWorkflowStatus() != WorkflowStatus.RUNNING) {
      boolean result = workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE;
      if (!result) {
        LOGGER.info("Claim for execution {} denied: workflow not in RUNNING or INQUEUE state.",
            workflowExecution.getId());
      }
      return result;
    }

    // If it is running, we check whether it is currently hanging. Get the map entry.
    final WorkflowExecutionEntry currentExecution = getEntry(workflowExecution);

    // If there is no entry, permission is denied: we assume one will appear shortly.
    if (currentExecution == null) {
      LOGGER.info(
          "Claim for execution {} denied: wait for scheduled monitoring task to monitor this RUNNING execution.",
          workflowExecution.getId());
      return false;
    }

    // Grant permission only if the execution appears to be hanging.
    final boolean result =
        currentExecution.updateTimeValueIsEqual(workflowExecution.getUpdatedDate())
            && currentExecution.assumeHanging(failsafeLeniency);
    if (!result) {
      LOGGER.info(
          "Claim for execution {} denied: RUNNING execution does not (yet) appear to be hanging.",
          workflowExecution.getId());
    }
    return result;
  }

  /* DO NOT CALL THIS METHOD WITHOUT POSSESSING THE LOCK */
  WorkflowExecutionEntry getEntry(WorkflowExecution workflowExecution) {
    return currentRunningExecutions.get(workflowExecution.getId().toString());
  }

  static class WorkflowExecutionEntry {

    /**
     * This is the date that other core instances may provide. Should be treated as a version
     * number: no time calculations should be done with this as the clock may differ from ours.
     **/
    private final Instant executionUpdateTime;

    /** This is the date on this machine. Can be treated as a time. **/
    private final Instant timeOfLastUpdateTimeChange;

    public WorkflowExecutionEntry(Date updateTime) {
      this.executionUpdateTime = updateTime == null ? null : updateTime.toInstant();
      this.timeOfLastUpdateTimeChange = Instant.now();
    }

    /**
     * Determines whether the given update time is equal to the one we know.
     * 
     * @param otherUpdateTime the update time to compare.
     * @return Whether it is equal to the one we have in the entry.
     */
    public boolean updateTimeValueIsEqual(Date otherUpdateTime) {
      Instant otherInstant = otherUpdateTime == null ? null : otherUpdateTime.toInstant();
      return Objects.equals(otherInstant, executionUpdateTime);
    }

    /**
     * Determines whether this workflow execution is hanging according to the given leniency. It is
     * assumed to be hanging if we obtained the last update more than the leniency period ago.
     * 
     * @param leniency The leniency with which to decide whether the execution is hanging.
     * @return Whether or not the execution is assumed to be hanging.
     */
    public boolean assumeHanging(Duration leniency) {
      return getLastValueChange().plus(leniency).isBefore(getNow());
    }

    public Instant getLastValueChange() {
      return timeOfLastUpdateTimeChange;
    }

    Instant getNow() {
      return Instant.now();
    }
  }
}
