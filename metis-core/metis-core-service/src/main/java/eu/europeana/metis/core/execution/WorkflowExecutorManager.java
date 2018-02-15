package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class WorkflowExecutorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutorManager.class);

  private int maxConcurrentThreads = 10; //Use setter otherwise default
  private int monitorCheckIntervalInSecs = 5; //Use setter otherwise default
  private int pollingTimeoutForCleaningCompletionServiceInSecs = 10; //Use setter otherwise default
  private final Channel rabbitmqChannel;
  private String rabbitmqQueueName; //Initialize with setter
  private int threadsCounter;

  private final ExecutorService threadPool = Executors.newFixedThreadPool(maxConcurrentThreads);
  private final ExecutorCompletionService<WorkflowExecution> completionService = new ExecutorCompletionService<>(
      threadPool);

  private final WorkflowExecutionDao workflowExecutionDao;
  private final RedissonClient redissonClient;
  private final DpsClient dpsClient;
  private String ecloudBaseUrl; //Initialize with setter
  private String ecloudProvider; //Initialize with setter

  @Autowired
  public WorkflowExecutorManager(
      WorkflowExecutionDao workflowExecutionDao, Channel rabbitmqChannel,
      RedissonClient redissonClient, DpsClient dpsClient) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.rabbitmqChannel = rabbitmqChannel;
    this.redissonClient = redissonClient;
    this.dpsClient = dpsClient;
  }

  public void initiateConsumer() throws IOException {
    //For correct priority. Keep in mind this pre-fetches a message before going into handleDelivery
    rabbitmqChannel.basicQos(1);
    //Auto acknowledge false(second parameter) because of Qos.
    rabbitmqChannel.basicConsume(rabbitmqQueueName, false, new QueueConsumer(rabbitmqChannel));
  }

  public void addWorkflowExecutionToQueue(String userWorkflowExecutionObjectId,
      int priority) {
    synchronized (this) {
      //Based on Rabbitmq the basicPublish between threads should be controlled(synchronized)
      BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
          .priority(priority)
          .build();
      try {
        //First parameter is the ExchangeName which is not used
        rabbitmqChannel.basicPublish("", rabbitmqQueueName, basicProperties,
            userWorkflowExecutionObjectId.getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        LOGGER.error("WorkflowExecution with objectId: {} not added in queue..",
            userWorkflowExecutionObjectId, e);
      }
    }
  }

  public synchronized void cancelWorkflowExecution(WorkflowExecution workflowExecution) {
    synchronized (this) {
      if (workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE
          || workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING) {
        workflowExecutionDao.setCancellingState(workflowExecution);
        LOGGER.info(
            "Cancelling user workflow execution with id: {}", workflowExecution.getId());
      }
    }
  }

  public void setRabbitmqQueueName(String rabbitmqQueueName) {
    this.rabbitmqQueueName = rabbitmqQueueName;
  }

  public void setMaxConcurrentThreads(int maxConcurrentThreads) {
    this.maxConcurrentThreads = maxConcurrentThreads;
  }

  public void setEcloudBaseUrl(String ecloudBaseUrl) {
    this.ecloudBaseUrl = ecloudBaseUrl;
  }

  public void setEcloudProvider(String ecloudProvider) {
    this.ecloudProvider = ecloudProvider;
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
      String objectId = new String(body, StandardCharsets.UTF_8);
      LOGGER.info("WorkflowExecution id: {} received from queue.", objectId);
      //Clean thread pool, some executions might have already finished
      if (threadsCounter >= maxConcurrentThreads) {
        LOGGER.debug("Trying to clean thread pool, found thread pool full with threadsCounter: {}, maxConcurrentThreads: {}",
            threadsCounter, maxConcurrentThreads);
        checkAndCleanCompletionService();
      }

      WorkflowExecution workflowExecution = workflowExecutionDao.getById(objectId);
      //If the thread pool is still full, executions are still active. Send the message back to the queue.
      if (threadsCounter >= maxConcurrentThreads) {
        //Send NACK to send message back to the queue. Message will go to the same position it was or as close as possible
        //NACK multiple(second parameter) we want one. Requeue(Third parameter), do not discard
        rabbitmqChannel
            .basicNack(rabbitmqEnvelope.getDeliveryTag(), false, true);
        LOGGER.debug("NACK sent for {} with tag {}", workflowExecution.getId(),
            rabbitmqEnvelope.getDeliveryTag());
      } else {
        if (!workflowExecution.isCancelling()) { //Submit for execution
          WorkflowExecutor workflowExecutor = new WorkflowExecutor(
              workflowExecution, workflowExecutionDao, monitorCheckIntervalInSecs,
              redissonClient, dpsClient, ecloudBaseUrl, ecloudProvider);
          completionService.submit(workflowExecutor);
          threadsCounter++;
        } else { //Has been cancelled, do not execute
          workflowExecution.setAllRunningAndInqueuePluginsToCancelled();
          workflowExecutionDao.update(workflowExecution);
          LOGGER.info("Cancelled inqueue user workflow execution with id: {}",
              workflowExecution.getId());
        }
        rabbitmqChannel
            .basicAck(rabbitmqEnvelope.getDeliveryTag(), false);//Send ACK back to remove from queue asap.
        LOGGER.debug("ACK sent for {} with tag {}", workflowExecution.getId(),
            rabbitmqEnvelope.getDeliveryTag());
      }
    }

    private void checkAndCleanCompletionService() throws IOException {
      //Block for a small period and try cleaning up
      try {
        Future<WorkflowExecution> userWorkflowExecutionFuture = completionService
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
