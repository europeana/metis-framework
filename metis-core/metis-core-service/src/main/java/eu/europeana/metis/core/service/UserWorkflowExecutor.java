package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
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

  private final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutor.class);
  private Date startDate;
  private Date finishDate;
  private boolean firstPluginExecution;
  private boolean cancelled = false;
  private final int iterationsToFake = 30;
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
    firstPluginExecution = true;
    startDate = new Date();
    userWorkflowExecution.setStartedDate(startDate);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    if (!cancelled && userWorkflowExecution.getVoidOaipmhHarvestPlugin() != null) {
      finishDate = runVoidOaipmhHarvestPlugin();
    } else if (!cancelled && userWorkflowExecution.getVoidHTTPHarvestPlugin() != null) {
      finishDate = runVoidHTTPHarvestPlugin();
    }

    if (!cancelled && userWorkflowExecution.getVoidDereferencePlugin() != null) {
      finishDate = runVoidDereferencePlugin();
    }
    if (!cancelled && userWorkflowExecution.getVoidMetisPlugin() != null) {
      finishDate = runVoidMetisPlugin();
    }

    if (cancelled) {
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
      if(userWorkflowExecution.getVoidDereferencePlugin().getPluginStatus() == PluginStatus.INQUEUE)
        userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.CANCELLED);
      if(userWorkflowExecution.getVoidMetisPlugin().getPluginStatus() == PluginStatus.INQUEUE)
        userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.CANCELLED);
      LOGGER.info("Cancelled user workflow execution with id: " + userWorkflowExecution.getId());
    } else {
      userWorkflowExecution.setFinishedDate(finishDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      LOGGER.info("Finished user workflow execution with id: " + userWorkflowExecution.getId());
    }
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution;
  }

  private Date runVoidHTTPHarvestPlugin() {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidHTTPHarvestPlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidHTTPHarvestPlugin().setStartedDate(new Date());
    }
    userWorkflowExecution.getVoidHTTPHarvestPlugin().setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    userWorkflowExecution.getVoidHTTPHarvestPlugin().execute();
    for (int i = 0; i < iterationsToFake; i++) {
      try {
        Thread.sleep(sleepTime);
        userWorkflowExecution.getVoidHTTPHarvestPlugin().monitor("");
        Date updatedDate = new Date();
        userWorkflowExecution.getVoidHTTPHarvestPlugin().setUpdatedDate(updatedDate);
        userWorkflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      } catch (InterruptedException e) {
        // TODO: 31-5-17 Call remote interruption cancelling
        cancelled = true;
        userWorkflowExecution.getVoidHTTPHarvestPlugin().setPluginStatus(PluginStatus.CANCELLED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        return userWorkflowExecution.getVoidHTTPHarvestPlugin().getFinishedDate();
      }
    }
    userWorkflowExecution.getVoidHTTPHarvestPlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidHTTPHarvestPlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution.getVoidHTTPHarvestPlugin().getFinishedDate();
  }

  private Date runVoidOaipmhHarvestPlugin() {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidOaipmhHarvestPlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidOaipmhHarvestPlugin().setStartedDate(new Date());
    }
    userWorkflowExecution.getVoidOaipmhHarvestPlugin().setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    userWorkflowExecution.getVoidOaipmhHarvestPlugin().execute();

    for (int i = 0; i < iterationsToFake; i++) {
      try {
        Thread.sleep(sleepTime);
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().monitor("");
        Date updatedDate = new Date();
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().setUpdatedDate(updatedDate);
        userWorkflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      } catch (InterruptedException e) {
        // TODO: 31-5-17 Call remote interruption cancelling
        cancelled = true;
        userWorkflowExecution.getVoidOaipmhHarvestPlugin().setPluginStatus(PluginStatus.CANCELLED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        return userWorkflowExecution.getVoidOaipmhHarvestPlugin().getFinishedDate();
      }
    }
    userWorkflowExecution.getVoidOaipmhHarvestPlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidOaipmhHarvestPlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution.getVoidOaipmhHarvestPlugin().getFinishedDate();
  }

  private Date runVoidDereferencePlugin() {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidDereferencePlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidDereferencePlugin().setStartedDate(new Date());
    }
    userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    userWorkflowExecution.getVoidDereferencePlugin().execute();
    for (int i = 0; i < iterationsToFake; i++) {
      try {
        Thread.sleep(sleepTime);
        userWorkflowExecution.getVoidDereferencePlugin().monitor("");
        Date updatedDate = new Date();
        userWorkflowExecution.getVoidDereferencePlugin().setUpdatedDate(updatedDate);
        userWorkflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      } catch (InterruptedException e) {
        // TODO: 31-5-17 Call remote interruption cancelling
        cancelled = true;
        userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.CANCELLED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        return userWorkflowExecution.getVoidDereferencePlugin().getFinishedDate();
      }
    }
    userWorkflowExecution.getVoidDereferencePlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidDereferencePlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution.getVoidDereferencePlugin().getFinishedDate();
  }

  private Date runVoidMetisPlugin() {
    if (firstPluginExecution) {
      firstPluginExecution = false;
      userWorkflowExecution.getVoidMetisPlugin().setStartedDate(startDate);
    } else {
      userWorkflowExecution.getVoidMetisPlugin().setStartedDate(new Date());
    }
    userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    userWorkflowExecution.getVoidMetisPlugin().execute();
    for (int i = 0; i < iterationsToFake; i++) {
      try {
        Thread.sleep(sleepTime);
        userWorkflowExecution.getVoidMetisPlugin().monitor("");
        Date updatedDate = new Date();
        userWorkflowExecution.getVoidMetisPlugin().setUpdatedDate(updatedDate);
        userWorkflowExecution.setUpdatedDate(updatedDate);
        userWorkflowExecutionDao.update(userWorkflowExecution);
      } catch (InterruptedException e) {
        // TODO: 31-5-17 Call remote interruption cancelling
        cancelled = true;
        userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.CANCELLED);
        userWorkflowExecutionDao.update(userWorkflowExecution);
        return userWorkflowExecution.getVoidMetisPlugin().getFinishedDate();
      }
    }
    userWorkflowExecution.getVoidMetisPlugin().setFinishedDate(new Date());
    userWorkflowExecution.getVoidMetisPlugin().setPluginStatus(PluginStatus.FINISHED);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    return userWorkflowExecution.getVoidMetisPlugin().getFinishedDate();
  }
}
