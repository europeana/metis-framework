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
  private Date startDate;
  private Date finishDate;
  private boolean firstPluginExecution;
  private final int secondsToFake = 10;
  private final int sleepTime = 1000;

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
    try {
      firstPluginExecution = true;
      startDate = new Date();
      userWorkflowExecution.setStartedDate(startDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
      userWorkflowExecutionDao.update(userWorkflowExecution);
      if (userWorkflowExecution.getVoidOaipmhHarvestPlugin() != null) {
        finishDate = runVoidOaipmhHarvestPlugin();
      } else if (userWorkflowExecution.getVoidHTTPHarvestPlugin() != null) {
        finishDate = runVoidHTTPHarvestPlugin();
      }

      if (userWorkflowExecution.getVoidDereferencePlugin() != null) {
        finishDate = runVoidDereferencePlugin();
      }
      if (userWorkflowExecution.getVoidMetisPlugin() != null) {
        finishDate = runVoidMetisPlugin();
      }
      userWorkflowExecution.setFinishedDate(finishDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      userWorkflowExecutionDao.update(userWorkflowExecution);
      LOGGER.info("Finished user workflow execution with id: " + userWorkflowExecution.getId());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return userWorkflowExecution;
  }

  private Date runVoidHTTPHarvestPlugin() throws InterruptedException {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidHTTPHarvestPlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidHTTPHarvestPlugin().setStartedDate(new Date());
    }
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
    userWorkflowExecution.getVoidHTTPHarvestPlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidHTTPHarvestPlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);

    return userWorkflowExecution.getVoidHTTPHarvestPlugin().getFinishedDate();
  }

  private Date runVoidOaipmhHarvestPlugin()
      throws InterruptedException {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidOaipmhHarvestPlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidOaipmhHarvestPlugin().setStartedDate(new Date());
    }
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
    userWorkflowExecution.getVoidOaipmhHarvestPlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidOaipmhHarvestPlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);

    return userWorkflowExecution.getVoidOaipmhHarvestPlugin().getFinishedDate();
  }

  private Date runVoidDereferencePlugin()
      throws InterruptedException {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidDereferencePlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidDereferencePlugin().setStartedDate(new Date());
    }
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
    userWorkflowExecution.getVoidDereferencePlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);

    return userWorkflowExecution.getVoidDereferencePlugin().getFinishedDate();
  }

  private Date runVoidMetisPlugin() throws InterruptedException {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidMetisPlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidMetisPlugin().setStartedDate(new Date());
    }
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
    userWorkflowExecution.getVoidMetisPlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);

    return userWorkflowExecution.getVoidMetisPlugin().getFinishedDate();
  }
}
