package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.util.Date;
import java.util.concurrent.Callable;
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

  private final UserWorkflowExecution userWorkflowExecution;
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;

  UserWorkflowExecutor(UserWorkflowExecution userWorkflowExecution,
      UserWorkflowExecutionDao userWorkflowExecutionDao) {
    this.userWorkflowExecution = userWorkflowExecution;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
  }

  @Override
  public UserWorkflowExecution call() {
    LOGGER.info("Starting user workflow execution with id: {} and priority {}", userWorkflowExecution.getId(), userWorkflowExecution.getWorkflowPriority());
    firstPluginExecution = true;
    if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE) {
      runInQueueStateWorkflowExecution();
    } else {
      runRunningStateWorkflowExecution();
    }

    if (userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())) {
      userWorkflowExecution.setAllRunningAndInqueuePluginsToCancelled();
      LOGGER.info(
          "Cancelled running user workflow execution with id: {}", userWorkflowExecution.getId());
    } else {
      userWorkflowExecution.setFinishedDate(finishDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      LOGGER.info("Finished user workflow execution with id: {}", userWorkflowExecution.getId());
    }
    //The only full update is used here. The rest of the execution uses partial updates to avoid losing the cancelling state field
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution;
  }

  private void runInQueueStateWorkflowExecution() {
    startDate = new Date();
    userWorkflowExecution.setStartedDate(startDate);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.updateMonitorInformation(userWorkflowExecution);
    for (AbstractMetisPlugin metisPlugin :
        userWorkflowExecution.getMetisPlugins()) {
      if (userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())) {
        break;
      }
      finishDate = runMetisPlugin(metisPlugin);
    }
  }

  private void runRunningStateWorkflowExecution() {
    //Run if the workflowExecution was retrieved from the queue in RUNNING state. Another process released it and came back into the queue
    int firstPluginPositionToStart = 0;
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
    int iterationsToFake = 5;
    int sleepTime = 5000;

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
        LOGGER.warn("Thread was interruped", e);
        return null;
      }
    }
    abstractMetisPlugin.setFinishedDate(new Date());
    abstractMetisPlugin.setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.updateWorkflowPlugins(userWorkflowExecution);
    return abstractMetisPlugin.getFinishedDate();
  }
}
