package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.Date;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class UserWorkflowExecutor implements Callable<UserWorkflowExecution> {
  private final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutor.class);

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
    final int secondsToFake = 30;
    final int sleepTime = 1000;
    try {
      userWorkflowExecution.setStartedDate(new Date());
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
      if (userWorkflowExecution.getVoidOaipmhHarvestPlugin() != null) {
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().setStartedDate(new Date());
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().setPluginStatus(PluginStatus.RUNNING);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().execute();

        for (int i = 0; i < secondsToFake; i++) {
          Thread.sleep(sleepTime);
          userWorkflowExecution.getVoidOaipmhHarvestPlugin().monitor("");
          Date updatedDate = new Date();
          userWorkflowExecution.getVoidOaipmhHarvestPlugin().setUpdatedDate(updatedDate);
          userWorkflowExecution.setUpdatedDate(updatedDate);
          userWorkflowExecutionDao.update(userWorkflowExecution);
        }
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().setPluginStatus(PluginStatus.FINISHED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      } else if (userWorkflowExecution.getVoidHTTPHarvestPlugin() != null) {
        userWorkflowExecution.getVoidHTTPHarvestPlugin().setStartedDate(new Date());
        userWorkflowExecution.getVoidHTTPHarvestPlugin().setPluginStatus(PluginStatus.RUNNING);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        userWorkflowExecution.getVoidHTTPHarvestPlugin().execute();
        for (int i = 0; i < secondsToFake; i++) {
          Thread.sleep(sleepTime);
          userWorkflowExecution.getVoidHTTPHarvestPlugin().monitor("");
          Date updatedDate = new Date();
          userWorkflowExecution.getVoidHTTPHarvestPlugin().setUpdatedDate(updatedDate);
          userWorkflowExecution.setUpdatedDate(updatedDate);
          userWorkflowExecutionDao.update(userWorkflowExecution);
        }
        userWorkflowExecution.getVoidHTTPHarvestPlugin().setPluginStatus(PluginStatus.FINISHED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      }

      if (userWorkflowExecution.getVoidDereferencePlugin() != null) {
        userWorkflowExecution.getVoidDereferencePlugin().setStartedDate(new Date());
        userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.RUNNING);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        userWorkflowExecution.getVoidDereferencePlugin().execute();
        for (int i = 0; i < secondsToFake; i++) {
          Thread.sleep(sleepTime);
          userWorkflowExecution.getVoidDereferencePlugin().monitor("");
          Date updatedDate = new Date();
          userWorkflowExecution.getVoidDereferencePlugin().setUpdatedDate(updatedDate);
          userWorkflowExecution.setUpdatedDate(updatedDate);
          userWorkflowExecutionDao.update(userWorkflowExecution);
        }
        userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.FINISHED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      }
      if (userWorkflowExecution.getVoidMetisPlugin() != null) {
        userWorkflowExecution.getVoidMetisPlugin().setStartedDate(new Date());
        userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.RUNNING);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        userWorkflowExecution.getVoidMetisPlugin().execute();
        for (int i = 0; i < secondsToFake; i++) {
          Thread.sleep(sleepTime);
          userWorkflowExecution.getVoidMetisPlugin().monitor("");
          Date updatedDate = new Date();
          userWorkflowExecution.getVoidMetisPlugin().setUpdatedDate(updatedDate);
          userWorkflowExecution.setUpdatedDate(updatedDate);
          userWorkflowExecutionDao.update(userWorkflowExecution);
        }
        userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.FINISHED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      }
      userWorkflowExecution.setFinishedDate(new Date());
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      userWorkflowExecutionDao.update(userWorkflowExecution);
      LOGGER.info("Finished user workflow execution with id: " + userWorkflowExecution.getId());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return userWorkflowExecution;
  }
}
