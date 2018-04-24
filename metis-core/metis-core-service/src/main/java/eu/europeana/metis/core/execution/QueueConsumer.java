package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-13
 */
public class QueueConsumer extends DefaultConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueConsumer.class);
  
  private final WorkflowExecutionSettings workflowExecutionSettings;
  private final PersistenceProvider persistenceProvider;

  private final ExecutorService threadPool;
  private final ExecutorCompletionService<WorkflowExecution> completionService;
  private int threadsCounter;

  QueueConsumer(WorkflowExecutionSettings workflowExecutionSettings,
      PersistenceProvider persistenceProvider) {
    super(persistenceProvider.getRabbitmqChannel());
    this.workflowExecutionSettings = workflowExecutionSettings;
    this.persistenceProvider = persistenceProvider;
    threadPool = Executors
        .newFixedThreadPool(this.workflowExecutionSettings.getMaxConcurrentThreads());
    completionService = new ExecutorCompletionService<>(threadPool);
  }

  //Does not run as a thread. Each execution will run separately one after the other for each consumption
  @Override
  public void handleDelivery(String consumerTag, Envelope rabbitmqEnvelope,
      AMQP.BasicProperties properties, byte[] body) throws IOException {
    String objectId = new String(body, StandardCharsets.UTF_8);
    LOGGER.info("WorkflowExecution id: {} received from queue.", objectId);
    //Clean thread pool, some executions might have already finished
    if (threadsCounter >= workflowExecutionSettings.getMaxConcurrentThreads()) {
      LOGGER.debug(
          "Trying to clean thread pool, found thread pool full with threadsCounter: {}, maxConcurrentThreads: {}",
          threadsCounter, workflowExecutionSettings.getMaxConcurrentThreads());
      checkAndCleanCompletionService();
    }

    WorkflowExecution workflowExecution = persistenceProvider.getWorkflowExecutionDao()
        .getById(objectId);
    //If the thread pool is still full, executions are still active. Send the message back to the queue.
    if (threadsCounter >= workflowExecutionSettings.getMaxConcurrentThreads()) {
      //Send NACK to send message back to the queue. Message will go to the same position it was or as close as possible
      //NACK multiple(second parameter) we want one. Requeue(Third parameter), do not discard
      persistenceProvider.getRabbitmqChannel()
          .basicNack(rabbitmqEnvelope.getDeliveryTag(), false, true);
      LOGGER.debug("NACK sent for {} with tag {}", workflowExecution.getId(),
          rabbitmqEnvelope.getDeliveryTag());
    } else {
      if (!workflowExecution.isCancelling()) { //Submit for execution
        WorkflowExecutor workflowExecutor = new WorkflowExecutor(
            workflowExecution, persistenceProvider.getWorkflowExecutionDao(),
            workflowExecutionSettings.getMonitorCheckIntervalInSecs(),
            persistenceProvider.getRedissonClient(), persistenceProvider.getDpsClient(),
            workflowExecutionSettings.getEcloudBaseUrl(), workflowExecutionSettings.getEcloudProvider());
        completionService.submit(workflowExecutor);
        threadsCounter++;
      } else { //Has been cancelled, do not execute
        workflowExecution.setAllRunningAndInqueuePluginsToCancelled();
        persistenceProvider.getWorkflowExecutionDao().update(workflowExecution);
        LOGGER.info("Cancelled inqueue user workflow execution with id: {}",
            workflowExecution.getId());
      }
      persistenceProvider.getRabbitmqChannel()
          .basicAck(rabbitmqEnvelope.getDeliveryTag(),
              false);//Send ACK back to remove from queue asap.
      LOGGER.debug("ACK sent for {} with tag {}", workflowExecution.getId(),
          rabbitmqEnvelope.getDeliveryTag());
    }
  }

  private void checkAndCleanCompletionService() throws IOException {
    //Block for a small period and try cleaning up
    try {
      Future<WorkflowExecution> userWorkflowExecutionFuture = completionService
          .poll(workflowExecutionSettings.getPollingTimeoutForCleaningCompletionServiceInSecs(),
              TimeUnit.SECONDS);
      if (userWorkflowExecutionFuture != null) {
        threadsCounter--;
      }
      while (completionService.poll() != null) {
        threadsCounter--;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error(
          "Interrupted while polling for taking a Future from the ExecutorCompletionService",
          e);
      throw new IOException(
          "Interrupted while polling for taking a Future from the ExecutorCompletionService", e);
    }
  }

  void close() {
    threadPool.shutdown();
  }

  int getThreadsCounter() {
    return threadsCounter;
  }
}
