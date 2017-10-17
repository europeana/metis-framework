package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-30
 */
@Component
public class UserWorkflowExecutorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutorManager.class);

  private int maxConcurrentThreads = 10; //Use setter otherwise default
  private int monitorCheckIntervalInSecs = 5; //Use setter otherwise default
  private int pollingTimeoutForCleaningCompletionServiceInSecs = 10; //Use setter otherwise default
  private final Channel rabbitmqChannel;
  private String rabbitmqQueueName; //Initialize with setter
  private int threadsCounter;

  private final ExecutorService threadPool = Executors.newFixedThreadPool(maxConcurrentThreads);
  private final ExecutorCompletionService<UserWorkflowExecution> completionService = new ExecutorCompletionService<>(
      threadPool);

  private final UserWorkflowExecutionDao userWorkflowExecutionDao;
  private final RedissonClient redissonClient;

  @Autowired
  public UserWorkflowExecutorManager(
      UserWorkflowExecutionDao userWorkflowExecutionDao, Channel rabbitmqChannel,
      RedissonClient redissonClient) {
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.rabbitmqChannel = rabbitmqChannel;
    this.redissonClient = redissonClient;
  }

  public void initiateConsumer() throws IOException {
    //For correct priority. Keep in mind this pre-fetches a message before going into handleDelivery
    rabbitmqChannel.basicQos(1);
    //Auto acknowledge false(second parameter) because of Qos.
    rabbitmqChannel.basicConsume(rabbitmqQueueName, false, new QueueConsumer(rabbitmqChannel));
  }

  public synchronized void addUserWorkflowExecutionToQueue(String userWorkflowExecutionObjectId,
      int priority) {
    //Based on Rabbitmq the basicPublish between threads should be controlled(synchronized)
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority)
        .build();
    try {
      //First parameter is the ExchangeName which is not used
      rabbitmqChannel.basicPublish("", rabbitmqQueueName, basicProperties,
          userWorkflowExecutionObjectId.getBytes("UTF-8"));
    } catch (IOException e) {
      LOGGER.error("UserWorkflowExecution with objectId: {} not added in queue..",
          userWorkflowExecutionObjectId, e);
    }
  }

  public void cancelUserWorkflowExecution(UserWorkflowExecution userWorkflowExecution) {
    if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE
        || userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING) {
      userWorkflowExecutionDao.setCancellingState(userWorkflowExecution);
      LOGGER.info(
          "Cancelling user workflow execution with id: {}", userWorkflowExecution.getId());
    }
  }

  public void setRabbitmqQueueName(String rabbitmqQueueName) {
    this.rabbitmqQueueName = rabbitmqQueueName;
  }

  public void setMaxConcurrentThreads(int maxConcurrentThreads) {
    this.maxConcurrentThreads = maxConcurrentThreads;
  }

  public int getMonitorCheckIntervalInSecs() {
    return monitorCheckIntervalInSecs;
  }

  public void setMonitorCheckIntervalInSecs(int monitorCheckIntervalInSecs) {
    this.monitorCheckIntervalInSecs = monitorCheckIntervalInSecs;
  }

  public int getThreadsCounter() {
    return threadsCounter;
  }

  public void setPollingTimeoutForCleaningCompletionServiceInSecs(
      int pollingTimeoutForCleaningCompletionServiceInSecs) {
    this.pollingTimeoutForCleaningCompletionServiceInSecs = pollingTimeoutForCleaningCompletionServiceInSecs;
  }

  @PreDestroy
  public void close() {
    threadPool.shutdown();
  }

  class QueueConsumer extends DefaultConsumer {

    QueueConsumer(Channel channel) {
      super(channel);
    }

    //Does not run as a thread. Each execution will run separately one after the other for each consumption
    @Override
    public void handleDelivery(String consumerTag, Envelope rabbitmqEnvelope,
        AMQP.BasicProperties properties, byte[] body) throws IOException {
      String objectId = new String(body, "UTF-8");
      LOGGER.info("UserWorkflowExecution id: {} received from queue.", objectId);
      UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
      //Clean thread pool, some executions might have already finished
      if (threadsCounter >= maxConcurrentThreads) {
        LOGGER.info("Trying to clean thread pool, found thread pool full with threadsCounter: {}, maxConcurrentThreads: {}",
            threadsCounter, maxConcurrentThreads);
        checkAndCleanCompletionService();
      }

      //If the thread pool is still full, executions are still active. Send the message back to the queue.
      if (threadsCounter >= maxConcurrentThreads) {
        //Send NACK to send message back to the queue. Message will go to the same position it was or as close as possible
        //NACK multiple(second parameter) we want one. Requeue(Third parameter), do not discard
        rabbitmqChannel
            .basicNack(rabbitmqEnvelope.getDeliveryTag(), false, true);
        LOGGER.info("NACK sent for {} with tag {}", userWorkflowExecution.getId(),
            rabbitmqEnvelope.getDeliveryTag());
      } else {
        if (!userWorkflowExecution.isCancelling()) { //Submit for execution
          UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(
              userWorkflowExecution, userWorkflowExecutionDao, monitorCheckIntervalInSecs,
              redissonClient);
          completionService.submit(userWorkflowExecutor);
          threadsCounter++;
        } else { //Has been cancelled, do not execute
          userWorkflowExecution.setAllRunningAndInqueuePluginsToCancelled();
          userWorkflowExecutionDao.update(userWorkflowExecution);
          LOGGER.info("Cancelled inqueue user workflow execution with id: {}",
              userWorkflowExecution.getId());
        }
        rabbitmqChannel
            .basicAck(rabbitmqEnvelope.getDeliveryTag(), false);//Send ACK back to remove from queue asap.
        LOGGER.info("ACK sent for {} with tag {}", userWorkflowExecution.getId(),
            rabbitmqEnvelope.getDeliveryTag());
      }
    }

    private void checkAndCleanCompletionService() throws IOException {
      //Block for a small period and try cleaning up
      try {
        Future<UserWorkflowExecution> userWorkflowExecutionFuture = completionService
            .poll(pollingTimeoutForCleaningCompletionServiceInSecs, TimeUnit.SECONDS);
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
        throw new IOException("Interrupted while polling for taking a Future from the ExecutorCompletionService", e);
      }
    }
  }
}
