package eu.europeana.metis.core.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.io.IOException;
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
  private final Channel rabbitmqChannel;
  private final Envelope rabbitmqItemEnvelope;

  UserWorkflowExecutor(
      UserWorkflowExecution userWorkflowExecution,
      UserWorkflowExecutionDao userWorkflowExecutionDao,
      Channel rabbitmqChannel, Envelope rabbitmqItemEnvelope) {
    this.userWorkflowExecution = userWorkflowExecution;
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.rabbitmqChannel = rabbitmqChannel;
    this.rabbitmqItemEnvelope = rabbitmqItemEnvelope;
  }

  @Override
  public UserWorkflowExecution call() {
    LOGGER.info("Starting user workflow execution with id: " + userWorkflowExecution.getId());
    firstPluginExecution = true;
    //Run if it's a workflow that is NOT in a RUNNING state
    if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE) {
      runInQueueStateWorkflowExecution();
    } else {
      runRunningStateWorkflowExecution();
    }

    if (cancelled) {
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
      for (AbstractMetisPlugin metisPlugin :
          userWorkflowExecution.getMetisPlugins()) {
        if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE) {
          metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
        }
      }
      LOGGER.info("Cancelled running user workflow execution with id: " + userWorkflowExecution.getId());
    } else {
      userWorkflowExecution.setFinishedDate(finishDate);
      userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
      LOGGER.info("Finished user workflow execution with id: " + userWorkflowExecution.getId());
    }
    userWorkflowExecutionDao.update(userWorkflowExecution);
    try {
      rabbitmqChannel.basicAck(rabbitmqItemEnvelope.getDeliveryTag(), false);
      LOGGER.debug("ACK sent for {}", userWorkflowExecution.getId());
    } catch (IOException e) {
      LOGGER.error("Could not send ACK of finished processing of item from queue.", e);
    }
    return userWorkflowExecution;
  }

  private void runInQueueStateWorkflowExecution() {
    startDate = new Date();
    userWorkflowExecution.setStartedDate(startDate);
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecutionDao.update(userWorkflowExecution);
    for (AbstractMetisPlugin metisPlugin :
        userWorkflowExecution.getMetisPlugins()) {
      if (cancelled) {
        break;
      }
      finishDate = runMetisPlugin(metisPlugin);
    }
  }

  private void runRunningStateWorkflowExecution() {
    //Run if the workflowExecution was retrieved from the queue in RUNNING state. Another process released it an came back into the queue
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
      if (cancelled) {
        break;
      }
      finishDate = runMetisPlugin(userWorkflowExecution.getMetisPlugins().get(i));
    }
  }

  private Date runMetisPlugin(AbstractMetisPlugin abstractMetisPlugin) {
    int iterationsToFake = 30;
    int sleepTime = 1000;

    //Check if the plugin had already set a starting date beforehand
    if (abstractMetisPlugin.getPluginStatus() == PluginStatus.INQUEUE) {
      if (firstPluginExecution) {
        firstPluginExecution = false;
        abstractMetisPlugin.setStartedDate(startDate);
      } else {
        abstractMetisPlugin.setStartedDate(new Date());
      }
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
