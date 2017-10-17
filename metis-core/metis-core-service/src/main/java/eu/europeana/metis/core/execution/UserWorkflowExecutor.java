package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
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
public class UserWorkflowExecutor implements Callable<UserWorkflowExecution> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutor.class);
  private Date startDate;
  private Date finishDate;
  private boolean firstPluginExecution;
  private int monitorCheckIntervalInSecs;

  private final UserWorkflowExecution userWorkflowExecution;
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;
  private final RedissonClient redissonClient;

  UserWorkflowExecutor(UserWorkflowExecution userWorkflowExecution,
      UserWorkflowExecutionDao userWorkflowExecutionDao, int monitorCheckIntervalInSecs,
      RedissonClient redissonClient) {
    this.userWorkflowExecution = userWorkflowExecution;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.monitorCheckIntervalInSecs = monitorCheckIntervalInSecs;
    this.redissonClient = redissonClient;
  }

  @Override
  public UserWorkflowExecution call() {
    //Get lock for the case that two cores will retrieve the same execution id from 2 items in the queue in different cores
    final String executionCheckLock = "executionCheckLock";
    RLock lock = redissonClient.getFairLock(executionCheckLock);
    LOGGER.info("Starting user workflow execution with id: {} and priority {}",
        userWorkflowExecution.getId(), userWorkflowExecution.getWorkflowPriority());
    firstPluginExecution = true;
    if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE
        && !userWorkflowExecutionDao
        .isExecutionActive(this.userWorkflowExecution, monitorCheckIntervalInSecs)) {
      runInQueueStateWorkflowExecution(lock);
    } else if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING
        && !userWorkflowExecutionDao
        .isExecutionActive(this.userWorkflowExecution, monitorCheckIntervalInSecs)) {
      runRunningStateWorkflowExecution(lock);
    } else {
      LOGGER.info(
          "Discarding user workflow execution with id: {}, it's not INQUEUE or RUNNNING and not active",
          userWorkflowExecution.getId());
      lock.unlock();
      return userWorkflowExecution;
    }

    if (userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())) {
      userWorkflowExecution.setAllRunningAndInqueuePluginsToCancelled();
      LOGGER.info(
          "Cancelled running user workflow execution with id: {}", userWorkflowExecution.getId());
    } else if (finishDate != null) { //finishedDate can be null in case of InterruptedException
      userWorkflowExecution.setFinishedDate(finishDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      LOGGER.info("Finished user workflow execution with id: {}", userWorkflowExecution.getId());
    }
    //The only full update is used here. The rest of the execution uses partial updates to avoid losing the cancelling state field
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution;
  }

  private void runInQueueStateWorkflowExecution(RLock lock) {
    startDate = new Date();
    userWorkflowExecution.setStartedDate(startDate);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.updateMonitorInformation(userWorkflowExecution);
    lock.unlock(); //Unlock as soon as possible
    for (AbstractMetisPlugin metisPlugin :
        userWorkflowExecution.getMetisPlugins()) {
      if (userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())) {
        break;
      }
      finishDate = runMetisPlugin(metisPlugin);
    }
  }

  private void runRunningStateWorkflowExecution(RLock lock) {
    //Run if the workflowExecution was retrieved from the queue in RUNNING state. Another process released it and came back into the queue
    userWorkflowExecution.setUpdatedDate(new Date());
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.updateMonitorInformation(userWorkflowExecution);
    lock.unlock(); //Unlock as soon as possible
    int firstPluginPositionToStart = 0;
    //Find the first plugin to continue execution from
    for (int i = 0; i < userWorkflowExecution.getMetisPlugins().size(); i++) {
      AbstractMetisPlugin metisPlugin = userWorkflowExecution.getMetisPlugins().get(i);
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING) {
        firstPluginPositionToStart = i;
        break;
      }
    }
    if (firstPluginPositionToStart != 0
        || userWorkflowExecution.getMetisPlugins().get(0).getPluginStatus()
        == PluginStatus.RUNNING) {
      firstPluginExecution = false;
    }
    for (int i = firstPluginPositionToStart; i < userWorkflowExecution.getMetisPlugins().size();
        i++) {
      if (userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())) {
        break;
      }
      finishDate = runMetisPlugin(userWorkflowExecution.getMetisPlugins().get(i));
    }
  }

  private Date runMetisPlugin(AbstractMetisPlugin abstractMetisPlugin) {
    int iterationsToFake = 2;
    int sleepTime = monitorCheckIntervalInSecs * 1000;

    if (abstractMetisPlugin.getPluginStatus() == PluginStatus.INQUEUE) {
      if (firstPluginExecution) {
        firstPluginExecution = false;
        abstractMetisPlugin.setStartedDate(startDate);
      } else {
        abstractMetisPlugin.setStartedDate(new Date());
      }
    }
    abstractMetisPlugin.setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.updateWorkflowPlugins(userWorkflowExecution);
    abstractMetisPlugin.execute();
    for (int i = 0; i < iterationsToFake; i++) {
      try {
        if (userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())) {
          return null;
        }
        Thread.sleep(sleepTime);
        abstractMetisPlugin.monitor("");
        Date updatedDate = new Date();
        abstractMetisPlugin.setUpdatedDate(updatedDate);
        userWorkflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.updateMonitorInformation(userWorkflowExecution);
      } catch (InterruptedException e) {
        LOGGER.warn("Thread was interrupted", e);
        Thread.currentThread().interrupt();
        return null;
      }
    }
    abstractMetisPlugin.setFinishedDate(new Date());
    abstractMetisPlugin.setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.updateWorkflowPlugins(userWorkflowExecution);
    return abstractMetisPlugin.getFinishedDate();
  }
}
