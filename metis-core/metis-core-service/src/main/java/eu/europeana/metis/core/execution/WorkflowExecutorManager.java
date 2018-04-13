package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
  private static final int DEFAULT_MAX_CONCURRENT_THREADS = 10;
  private static final int DEFAULT_MONITOR_CHECK_INTERVAL_IN_SECS = 5;
  private static final int DEFAULT_POLLING_TIMEOUT_FOR_CLEANING_COMPLETION_SERVICE_IN_SECS = 10;

  private int maxConcurrentThreads = DEFAULT_MAX_CONCURRENT_THREADS; //Use setter otherwise default
  private int monitorCheckIntervalInSecs = DEFAULT_MONITOR_CHECK_INTERVAL_IN_SECS; //Use setter otherwise default
  private int pollingTimeoutForCleaningCompletionServiceInSecs = DEFAULT_POLLING_TIMEOUT_FOR_CLEANING_COMPLETION_SERVICE_IN_SECS; //Use setter otherwise default
  private final Channel rabbitmqChannel;
  private String rabbitmqQueueName; //Initialize with setter

  private final WorkflowExecutionDao workflowExecutionDao;
  private final RedissonClient redissonClient;
  private final DpsClient dpsClient;
  private String ecloudBaseUrl; //Initialize with setter
  private String ecloudProvider; //Initialize with setter
  private QueueConsumer queueConsumer;

  /**
   * Autowired constructor.
   *
   * @param workflowExecutionDao the DAO for accessing WorkflowExecutions
   * @param rabbitmqChannel the channel for connecting to RabbitMQ
   * @param redissonClient the redisson client for distributed locks
   * @param dpsClient the Data Processing Service client from ECloud
   */
  @Autowired
  public WorkflowExecutorManager(
      WorkflowExecutionDao workflowExecutionDao, Channel rabbitmqChannel,
      RedissonClient redissonClient, DpsClient dpsClient) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.rabbitmqChannel = rabbitmqChannel;
    this.redissonClient = redissonClient;
    this.dpsClient = dpsClient;
    queueConsumer = new QueueConsumer(this); //Keep the reference to be able to close the resources on shutdown
  }

  /**
   * Initializes the consumer client for the distributed queue.
   *
   * @throws IOException if rabbitMQ consumer client initialization fails
   */
  public void initiateConsumer() throws IOException {
    //For correct priority. Keep in mind this pre-fetches a message before going into handleDelivery
    rabbitmqChannel.basicQos(1);
    //Auto acknowledge false(second parameter) because of Qos.
    rabbitmqChannel.basicConsume(rabbitmqQueueName, false, queueConsumer);
  }

  /**
   * Adds a WorkfloExecution identifier in the distributed queue.
   *
   * @param userWorkflowExecutionObjectId the WorkflowExecution identifier
   * @param priority the priority of the WorkflowExecution in the queue
   */
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

  public void setPollingTimeoutForCleaningCompletionServiceInSecs(
      int pollingTimeoutForCleaningCompletionServiceInSecs) {
    this.pollingTimeoutForCleaningCompletionServiceInSecs = pollingTimeoutForCleaningCompletionServiceInSecs;
  }

  int getMaxConcurrentThreads() {
    return maxConcurrentThreads;
  }

  int getPollingTimeoutForCleaningCompletionServiceInSecs() {
    return pollingTimeoutForCleaningCompletionServiceInSecs;
  }

  public WorkflowExecutionDao getWorkflowExecutionDao() {
    return workflowExecutionDao;
  }

  DpsClient getDpsClient() {
    return dpsClient;
  }

  public RedissonClient getRedissonClient() {
    return redissonClient;
  }

  Channel getRabbitmqChannel() {
    return rabbitmqChannel;
  }

  String getEcloudBaseUrl() {
    return ecloudBaseUrl;
  }

  String getEcloudProvider() {
    return ecloudProvider;
  }

  /**
   * Close any open resources
   */
  @PreDestroy
  public void close() {
    queueConsumer.close();
  }
}
