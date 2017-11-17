package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.util.Date;
import java.util.concurrent.Callable;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class UserWorkflowExecutor implements Callable<WorkflowExecution> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutor.class);
  private Date startDate;
  private Date finishDate;
  private boolean firstPluginExecution;
  private int monitorCheckIntervalInSecs;

  private final WorkflowExecution workflowExecution;
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;
  private final RedissonClient redissonClient;

  UserWorkflowExecutor(WorkflowExecution workflowExecution,
      UserWorkflowExecutionDao userWorkflowExecutionDao, int monitorCheckIntervalInSecs,
      RedissonClient redissonClient) {
    this.workflowExecution = workflowExecution;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.monitorCheckIntervalInSecs = monitorCheckIntervalInSecs;
    this.redissonClient = redissonClient;
  }

  @Override
  public WorkflowExecution call() {
    //Get lock for the case that two cores will retrieve the same execution id from 2 items in the queue in different cores
    final String executionCheckLock = "executionCheckLock";
    RLock lock = redissonClient.getFairLock(executionCheckLock);
    lock.lock();
    LOGGER.info("Starting user workflow execution with id: {} and priority {}",
        workflowExecution.getId(), workflowExecution.getWorkflowPriority());
    firstPluginExecution = true;
    if (workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE
        && !userWorkflowExecutionDao
        .isExecutionActive(this.workflowExecution, monitorCheckIntervalInSecs)) {
      runInQueueStateWorkflowExecution(lock);
    } else if (workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING
        && !userWorkflowExecutionDao
        .isExecutionActive(this.workflowExecution, monitorCheckIntervalInSecs)) {
      runRunningStateWorkflowExecution(lock);
    } else {
      LOGGER.info(
          "Discarding user workflow execution with id: {}, it's not INQUEUE or RUNNNING and not active",
          workflowExecution.getId());
      lock.unlock();
      return workflowExecution;
    }

    //Cancel workflow and all other than finished plugins if the workflow was cancelled during execution
    if (userWorkflowExecutionDao.isCancelling(workflowExecution.getId())) {
      workflowExecution.setAllRunningAndInqueuePluginsToCancelled();
      LOGGER.info(
          "Cancelled running user workflow execution with id: {}", workflowExecution.getId());
    } else if (finishDate != null) { //finishedDate can be null in case of InterruptedException
      workflowExecution.setFinishedDate(finishDate);
      workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      LOGGER.info("Finished user workflow execution with id: {}", workflowExecution.getId());
    }
    //The only full update is used here. The rest of the execution uses partial updates to avoid losing the cancelling state field
    userWorkflowExecutionDao.update(workflowExecution);
    return workflowExecution;
  }

  private void runInQueueStateWorkflowExecution(RLock lock) {
    // TODO: 16-11-17 Create ecloud dataset if it doesn't already exist for metis dataset
    startDate = new Date();
    workflowExecution.setStartedDate(startDate);
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.updateMonitorInformation(workflowExecution);
    lock.unlock(); //Unlock as soon as possible
    for (AbstractMetisPlugin metisPlugin :
        workflowExecution.getMetisPlugins()) {
      if (userWorkflowExecutionDao.isCancelling(workflowExecution.getId())) {
        break;
      }
      finishDate = runMetisPlugin(metisPlugin);
    }
  }

  private void runRunningStateWorkflowExecution(RLock lock) {
    //Run if the workflowExecution was retrieved from the queue in RUNNING state. Another process released it and came back into the queue
    workflowExecution.setUpdatedDate(new Date());
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.updateMonitorInformation(workflowExecution);
    lock.unlock(); //Unlock as soon as possible
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
        || workflowExecution.getMetisPlugins().get(0).getPluginStatus()
        == PluginStatus.RUNNING) {
      firstPluginExecution = false;
    }
    for (int i = firstPluginPositionToStart; i < workflowExecution.getMetisPlugins().size();
        i++) {
      if (userWorkflowExecutionDao.isCancelling(workflowExecution.getId())) {
        break;
      }
      finishDate = runMetisPlugin(workflowExecution.getMetisPlugins().get(i));
    }
  }

  private Date runMetisPlugin(AbstractMetisPlugin abstractMetisPlugin) {
    int iterationsToFake = 2;
    int sleepTime = monitorCheckIntervalInSecs * 1000;

    //Determinned the startedDate for the plugin
    if (abstractMetisPlugin.getPluginStatus() == PluginStatus.INQUEUE) {
      if (firstPluginExecution) {
        firstPluginExecution = false;
        abstractMetisPlugin.setStartedDate(startDate);
      } else {
        abstractMetisPlugin.setStartedDate(new Date());
      }
    }
    abstractMetisPlugin.setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    //Start execution and periodical check
    abstractMetisPlugin.execute();
    for (int i = 0; i < iterationsToFake; i++) {
      try {
        if (userWorkflowExecutionDao.isCancelling(workflowExecution.getId())) {
          return null;
        }
        Thread.sleep(sleepTime);
        abstractMetisPlugin.monitor("");
        Date updatedDate = new Date();
        abstractMetisPlugin.setUpdatedDate(updatedDate);
        workflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.updateMonitorInformation(workflowExecution);
      } catch (InterruptedException e) {
        LOGGER.warn("Thread was interrupted", e);
        Thread.currentThread().interrupt();
        return null;
      }
    }
    abstractMetisPlugin.setFinishedDate(new Date());
    abstractMetisPlugin.setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.updateWorkflowPlugins(workflowExecution);
    return abstractMetisPlugin.getFinishedDate();
  }
}
