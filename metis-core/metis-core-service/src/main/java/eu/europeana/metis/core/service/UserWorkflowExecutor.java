package eu.europeana.metis.core.service;

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
  private boolean cancelled = false;

  private final UserWorkflowExecution userWorkflowExecution;
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;

  public UserWorkflowExecutor(
      UserWorkflowExecution userWorkflowExecution,
      UserWorkflowExecutionDao userWorkflowExecutionDao) {
    this.userWorkflowExecution = userWorkflowExecution;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
  }

  @Override
  public UserWorkflowExecution call() {
    LOGGER.info("Starting user workflow execution with id: " + userWorkflowExecution.getId());
    firstPluginExecution = true;
    startDate = new Date();
    userWorkflowExecution.setStartedDate(startDate);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    for (AbstractMetisPlugin metisPlugin :
        userWorkflowExecution.getMetisPlugins()) {
      if (!cancelled) {
        finishDate = runMetisPlugin(metisPlugin);
      } else {
        break;
      }
    }

    if (cancelled) {
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
      for (AbstractMetisPlugin metisPlugin :
          userWorkflowExecution.getMetisPlugins()) {
        if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE) {
          metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
        }
      }
      LOGGER.info("Cancelled user workflow execution with id: " + userWorkflowExecution.getId());
    } else {
      userWorkflowExecution.setFinishedDate(finishDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      LOGGER.info("Finished user workflow execution with id: " + userWorkflowExecution.getId());
    }
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution;
  }

  private Date runMetisPlugin(AbstractMetisPlugin abstractMetisPlugin) {
    int iterationsToFake = 30;
    int sleepTime = 1000;

    if (firstPluginExecution) {
      firstPluginExecution = false;
      abstractMetisPlugin.setStartedDate(startDate);
    } else {
      abstractMetisPlugin.setStartedDate(new Date());
    }
    abstractMetisPlugin.setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    abstractMetisPlugin.execute();
    for (int i = 0; i < iterationsToFake; i++) {
      try {
        Thread.sleep(sleepTime);
        abstractMetisPlugin.monitor("");
        Date updatedDate = new Date();
        abstractMetisPlugin.setUpdatedDate(updatedDate);
        userWorkflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      } catch (InterruptedException e) {
        // TODO: 31-5-17 Call remote interruption cancelling
        cancelled = true;
        abstractMetisPlugin.setPluginStatus(PluginStatus.CANCELLED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        return abstractMetisPlugin.getFinishedDate();
      }
    }
    abstractMetisPlugin.setFinishedDate(new Date());
    abstractMetisPlugin.setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return abstractMetisPlugin.getFinishedDate();
  }
}
