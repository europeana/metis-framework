package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Class that handles the initializing connection to the RabbitMQ distributed queue and handling the
 * consuming of items from the queue, through the implemented {@link #handleDelivery(String,
 * Envelope, BasicProperties, byte[])} method.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-13
 */
@EnableScheduling
public class QueueConsumer extends DefaultConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueConsumer.class);

  private final WorkflowExecutionSettings workflowExecutionSettings;
  private final WorkflowExecutorManager workflowExecutorManager;
  private final WorkflowExecutionMonitor workflowExecutionMonitor;

  private final ExecutorService threadPool;
  private final ExecutorCompletionService<Pair<WorkflowExecution, Boolean>> completionService;
  private int threadsCounter;

  /**
   * Constructor with all required parameters to initialize the consumer connection to the
   * distributed queue and initialize the execution pool
   *
   * @param rabbitmqConsumerChannel the consumer channel of the queue
   * @param rabbitmqQueueName the queue name
   * @param workflowExecutionSettings the object that contains execution related settings
   * @param workflowExecutorManager the object that contains persistence related objects
   * @param workflowExecutionMonitor the object used to monitor executions
   * @throws IOException if the consumer channel initialization fails
   */
  public QueueConsumer(Channel rabbitmqConsumerChannel, String rabbitmqQueueName,
      WorkflowExecutionSettings workflowExecutionSettings,
      WorkflowExecutorManager workflowExecutorManager,
      WorkflowExecutionMonitor workflowExecutionMonitor) throws IOException {
    super(workflowExecutorManager.getRabbitmqConsumerChannel());
    this.workflowExecutionSettings = workflowExecutionSettings;
    this.workflowExecutorManager = workflowExecutorManager;
    threadPool = Executors.newCachedThreadPool();
    completionService = new ExecutorCompletionService<>(threadPool);
    this.workflowExecutionMonitor = workflowExecutionMonitor;

    // For correct priority. Keep in mind this pre-fetches a message before going into
    // handleDelivery
    rabbitmqConsumerChannel.basicQos(1);
    // Auto acknowledge false(second parameter) because of Qos.
    rabbitmqConsumerChannel.basicConsume(rabbitmqQueueName, false, this);
  }

  //Does not run as a thread. Each execution will run separately one after the other for each consumption
  //Make sure that if an exception occurs from mongo connections, the related "current" execution is safe
  //to not be processed in this run and will be picked up on a later stage. See also the configuration of
  //the related ConnectionFactory.
  @Override
  public void handleDelivery(String consumerTag, Envelope rabbitmqEnvelope,
      AMQP.BasicProperties properties, byte[] body) throws IOException {
    String objectId = new String(body, StandardCharsets.UTF_8);
    LOGGER.info("WorkflowExecution id: {} received from queue.", objectId);

    try {
      WorkflowExecution workflowExecution = workflowExecutorManager.getWorkflowExecutionDao()
          .getById(objectId);
      if (workflowExecution == null) {
        // This execution no longer exists and we need to ignore it.
        LOGGER.warn("Workflow execution with id: {} is in queue but no longer exists.", objectId);
      } else if (workflowExecution.isCancelling()) {
        // Has been cancelled, do not execute
        workflowExecution.setWorkflowAndAllQualifiedPluginsToCancelled();
        workflowExecutorManager.getWorkflowExecutionDao().update(workflowExecution);
        LOGGER.info("Cancelled inqueue user workflow execution with id: {}",
            workflowExecution.getId());
      } else {
        submitExecution(objectId);
      }
    } catch (RuntimeException e) {
      LOGGER.error(
          "Exception occurred during submitting message from queue to a workflowExecution for id {}",
          objectId, e);
    } finally {
      sendAck(rabbitmqEnvelope, objectId);
    }
  }

  private void submitExecution(String objectId) {
    WorkflowExecutor workflowExecutor = new WorkflowExecutor(objectId, workflowExecutorManager,
        workflowExecutionSettings, workflowExecutionMonitor);
    completionService.submit(workflowExecutor);
    threadsCounter++;
  }

  private void sendAck(Envelope rabbitmqEnvelope, String objectId) throws IOException {
    // Send ACK back to remove from queue asap.
    super.getChannel().basicAck(rabbitmqEnvelope.getDeliveryTag(), false);
    LOGGER.debug("ACK sent for {} with tag {}", objectId, rabbitmqEnvelope.getDeliveryTag());
  }

  @Scheduled(fixedDelay = 5000)
  private void checkAndCleanCompletionService() throws IOException {
    LOGGER.debug("Wait till next task finishes, threadsCounter: {}", threadsCounter);
    try {
      Future<Pair<WorkflowExecution, Boolean>> userWorkflowExecutionFuture = completionService
          .poll();
      while (userWorkflowExecutionFuture != null) {
        threadsCounter--;
        final WorkflowExecution workflowExecution = userWorkflowExecutionFuture.get().getLeft();
        if (workflowExecution != null) {
          boolean wasExecutionClaimedAndPluginRan = userWorkflowExecutionFuture.get().getRight();
          LOGGER.debug("workflowExecutionId: {} - Task finished", workflowExecution.getId());
          //If a plugin did not run, we are sending it back to queue so another instance can pick it up
          if (!wasExecutionClaimedAndPluginRan) {
            LOGGER.debug("workflowExecutionId: {} - Sent to queue because execution could "
                    + "not be claimed or plugin could not run in this instance",
                workflowExecution.getId());
            workflowExecutorManager
                .addWorkflowExecutionToQueue(workflowExecution.getId().toString(),
                    workflowExecution.getWorkflowPriority());
          }
        }
        userWorkflowExecutionFuture = completionService.poll();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(
          "Interrupted while polling for taking a Future from the ExecutorCompletionService", e);
    } catch (ExecutionException e) {
      throw new IOException("Exception occurred in Future task", e);
    }
  }

  @PreDestroy
  void close() {
    threadPool.shutdown();
  }

  int getThreadsCounter() {
    return threadsCounter;
  }
}
