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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles the initializing connection to the RabbitMQ distributed queue and handling the
 * consuming of items from the queue, through the implemented {@link #handleDelivery(String,
 * Envelope, BasicProperties, byte[])} method.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-13
 */
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

  /**
   * Handles each consumed message from the queue.
   * <p>
   * Does not run as a thread. Each execution will run separately one after the other for each
   * consumption. Make sure that if an exception occurs from mongo connections, the related
   * "current" execution is safe to not be processed in this run and will be picked up on a later
   * stage. See also the configuration of the related {@link com.rabbitmq.client.ConnectionFactory}.
   * </p>
   * <p>
   * Each message consumed is a workflow execution identifier which is used to claim and retrieve a
   * {@link WorkflowExecution} from the database. That workflow execution is then provided to a
   * {@link WorkflowExecutor} which is a {@link java.util.concurrent.Callable} and is in turn
   * submitted to the {@link ExecutorCompletionService} in this class. Every message retrieved is
   * ACked and therefore removed from the queue.
   * </p>
   * <p>
   * Cleanup and identification of submitted and finished tasks is controlled in the method {@link
   * #checkAndCleanCompletionService}. This method <b>SHOULD</b> be ran periodically from wherever
   * an instance of this class is used. It is not thread safe.
   * </p>
   *
   * @param consumerTag the consumer tage
   * @param rabbitmqEnvelope the rabbitmq envelope
   * @param properties the queue properties
   * @param body the body of the consumed message
   * @throws IOException if an exception occurred while sending an ACK back to the queue
   */
  @Override
  public void handleDelivery(String consumerTag, Envelope rabbitmqEnvelope,
      AMQP.BasicProperties properties, byte[] body) throws IOException {
    String objectId = new String(body, StandardCharsets.UTF_8);
    LOGGER.info("workflowExecutionId: {} - Received from queue.", objectId);
    LOGGER.info("workflowExecutionId: {} - Claiming workflow execution", objectId);
    Pair<WorkflowExecution, Boolean> workflowExecutionClaimedPair = workflowExecutionMonitor
        .claimExecution(objectId);
    WorkflowExecution workflowExecution = workflowExecutionClaimedPair.getLeft();
    final Boolean workflowClaimed = workflowExecutionClaimedPair.getRight();

    try {
      if (workflowExecution == null) {
        // This execution no longer exists and we need to ignore it.
        LOGGER.warn("workflowExecutionId: {} - Was in queue but no longer exists.", objectId);
      } else if (workflowClaimed.equals(Boolean.TRUE)) {
        LOGGER.info("workflowExecutionId: {} - Claimed", workflowExecution.getId());
        handleClaimedExecution(workflowExecution);
      } else if (workflowClaimed.equals(Boolean.FALSE)
          && WorkflowExecutionMonitor.CLAIMABLE_STATUSES
          .contains(workflowExecution.getWorkflowStatus())) {
        LOGGER.info("workflowExecutionId: {} - Could not be claimed, discarding message",
            workflowExecution.getId());
      } else {
        LOGGER
            .info("workflowExecutionId: {} - Does not have a claimable status, discarding message",
                workflowExecution.getId());
      }
    } finally {
      sendAck(rabbitmqEnvelope, objectId);
    }
  }

  private void handleClaimedExecution(WorkflowExecution workflowExecution) {
    try {
      if (workflowExecution.isCancelling()) {
        // Has been cancelled, do not execute
        workflowExecution.setWorkflowAndAllQualifiedPluginsToCancelled();
        workflowExecutorManager.getWorkflowExecutionDao().update(workflowExecution);
        LOGGER.info("workflowExecutionId: {} - Cancelled", workflowExecution.getId());
      } else {
        submitExecution(workflowExecution);
      }
    } catch (RuntimeException e) {
      LOGGER.error(String.format(
          "workflowExecutionId: %s - Exception occurred during submitting message from queue",
          workflowExecution.getId()), e);
    }
  }

  private void submitExecution(WorkflowExecution workflowExecution) {
    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutorManager, workflowExecutionSettings);
    completionService.submit(workflowExecutor);
    threadsCounter++;
  }

  private void sendAck(Envelope rabbitmqEnvelope, String objectId) throws IOException {
    // Send ACK back to remove from queue asap.
    super.getChannel().basicAck(rabbitmqEnvelope.getDeliveryTag(), false);
    LOGGER.debug("workflowExecutionId: {} - ACK sent with tag {}", objectId,
        rabbitmqEnvelope.getDeliveryTag());
  }

  /**
   * Polls the completion service until there isn't any result returned.
   * <p>Based on the result returned from the completion service, this method will decide if a
   * workflow execution should be sent back to the queue.</p>
   * <p>This method <b>SHOULD</b> be ran periodically from
   * wherever an instance of this class is used. It is not thread safe.</p>
   *
   * @throws InterruptedException if the execution of this method was interrupted
   */
  public void checkAndCleanCompletionService() throws InterruptedException {
    LOGGER.debug("Check if we have a task that has finished, threadsCounter: {}", threadsCounter);
    Future<Pair<WorkflowExecution, Boolean>> userWorkflowExecutionFuture = completionService.poll();
    while (userWorkflowExecutionFuture != null) {
      threadsCounter--;
      try {
        checkCollectedWorkflowExecution(userWorkflowExecutionFuture.get());
      } catch (ExecutionException e) {
        LOGGER.warn("Exception occurred in Future task", e);
      }
      userWorkflowExecutionFuture = completionService.poll();
    }

  }

  /**
   * Checks if the workflow execution was run as expected.
   * <p>
   * If one of the plugins was not allowed to run therefore the workflow execution did not complete
   * as a whole then we are resending the execution identifier back to the queue. If this execution
   * needs to be prioritized then the priority should be updated inside the {@link
   * java.util.concurrent.Callable}
   * </p>
   *
   * @param workflowExecutionRanFlagPair the workflow execution future
   */
  private void checkCollectedWorkflowExecution(
      Pair<WorkflowExecution, Boolean> workflowExecutionRanFlagPair) {
    final WorkflowExecution workflowExecution = workflowExecutionRanFlagPair.getLeft();
    if (workflowExecution != null) {
      boolean wasExecutionClaimedAndPluginRan = workflowExecutionRanFlagPair.getRight();
      //If a plugin did not run, we are sending it back to queue so another instance can pick it up
      if (wasExecutionClaimedAndPluginRan) {
        LOGGER.info("workflowExecutionId: {} - Task finished", workflowExecution.getId());
      } else {
        LOGGER.info("workflowExecutionId: {} - Sent to queue because execution could "
            + "not be claimed or plugin could not run in this instance", workflowExecution.getId());
        workflowExecutorManager.addWorkflowExecutionToQueue(workflowExecution.getId().toString(),
            workflowExecution.getWorkflowPriority());
      }
    }
  }

  /**
   * Close resources
   */
  public void close() {
    threadPool.shutdown();
    //Interrupt running threads
    threadPool.shutdownNow();
  }

  int getThreadsCounter() {
    return threadsCounter;
  }
}
