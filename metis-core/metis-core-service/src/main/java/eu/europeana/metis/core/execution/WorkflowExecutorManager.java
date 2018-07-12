package eu.europeana.metis.core.execution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-30
 */
public class WorkflowExecutorManager extends PersistenceProvider implements
    WorkflowExecutionSettings {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutorManager.class);
  private static final int DEFAULT_MAX_CONCURRENT_THREADS = 10;
  private static final int DEFAULT_MONITOR_CHECK_INTERVAL_IN_SECS = 5;
  private static final int DEFAULT_POLLING_TIMEOUT_FOR_CLEANING_COMPLETION_SERVICE_IN_SECS = 10;

  private int maxConcurrentThreads = DEFAULT_MAX_CONCURRENT_THREADS; //Use setter otherwise default
  private int dpsMonitorCheckIntervalInSecs = DEFAULT_MONITOR_CHECK_INTERVAL_IN_SECS; //Use setter otherwise default
  private int pollingTimeoutForCleaningCompletionServiceInSecs = DEFAULT_POLLING_TIMEOUT_FOR_CLEANING_COMPLETION_SERVICE_IN_SECS; //Use setter otherwise default

  private String rabbitmqQueueName; //Initialize with setter
  private String ecloudBaseUrl; //Initialize with setter
  private String ecloudProvider; //Initialize with setter

  /**
   * Autowired constructor.
   *
   * @param workflowExecutionDao the DAO for accessing WorkflowExecutions
   * @param rabbitmqPublisherChannel the channel for publishing to RabbitMQ
   * @param rabbitmqConsumerChannel the channel for consuming from RabbitMQ
   * @param redissonClient the redisson client for distributed locks
   * @param dpsClient the Data Processing Service client from ECloud
   */
  public WorkflowExecutorManager(
      WorkflowExecutionDao workflowExecutionDao, Channel rabbitmqPublisherChannel,
      Channel rabbitmqConsumerChannel, RedissonClient redissonClient, DpsClient dpsClient) {
    super(rabbitmqPublisherChannel, rabbitmqConsumerChannel, workflowExecutionDao, redissonClient,
        dpsClient);
  }

  /**
   * Adds a WorkflowExecution identifier in the distributed queue.
   *
   * @param userWorkflowExecutionObjectId the WorkflowExecution identifier
   * @param priority the priority of the WorkflowExecution in the queue
   */
  public void addWorkflowExecutionToQueue(String userWorkflowExecutionObjectId, int priority) {
    synchronized (getRabbitmqPublisherChannel()) {
      //Based on Rabbitmq the basicPublish between threads should be controlled(synchronized)
      BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
          .priority(priority).build();
      try {
        //First parameter is the ExchangeName which is not used
        getRabbitmqPublisherChannel().basicPublish("", rabbitmqQueueName, basicProperties,
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

  public void setDpsMonitorCheckIntervalInSecs(int dpsMonitorCheckIntervalInSecs) {
    this.dpsMonitorCheckIntervalInSecs = dpsMonitorCheckIntervalInSecs;
  }

  public void setPollingTimeoutForCleaningCompletionServiceInSecs(
      int pollingTimeoutForCleaningCompletionServiceInSecs) {
    this.pollingTimeoutForCleaningCompletionServiceInSecs = pollingTimeoutForCleaningCompletionServiceInSecs;
  }

  @Override
  public int getDpsMonitorCheckIntervalInSecs() {
    return dpsMonitorCheckIntervalInSecs;
  }

  @Override
  public int getMaxConcurrentThreads() {
    return maxConcurrentThreads;
  }

  @Override
  public int getPollingTimeoutForCleaningCompletionServiceInSecs() {
    return pollingTimeoutForCleaningCompletionServiceInSecs;
  }

  @Override
  public String getEcloudBaseUrl() {
    return ecloudBaseUrl;
  }

  @Override
  public String getEcloudProvider() {
    return ecloudProvider;
  }
}
