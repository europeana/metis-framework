package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import eu.europeana.metis.core.workflow.plugins.ThrottlingValues;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class for adding executions in the distributed queue.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-30
 */
public class WorkflowExecutorManager extends PersistenceProvider implements
    WorkflowExecutionSettings {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutorManager.class);
  private static final int DEFAULT_MONITOR_CHECK_INTERVAL_IN_SECS = 5;
  private static final int DEFAULT_PERIOD_OF_NO_PROCESSED_RECORDS_CHANGE_IN_MINUTES = 30;

  private int dpsMonitorCheckIntervalInSecs = DEFAULT_MONITOR_CHECK_INTERVAL_IN_SECS; //Use setter otherwise default
  private int periodOfNoProcessedRecordsChangeInMinutes = DEFAULT_PERIOD_OF_NO_PROCESSED_RECORDS_CHANGE_IN_MINUTES; //Use setter otherwise default

  private String rabbitmqQueueName; //Initialize with setter
  private String ecloudBaseUrl; //Initialize with setter
  private String ecloudProvider; //Initialize with setter
  private String metisCoreBaseUrl; //Initialize with setter
  private ThrottlingValues throttlingValues; //Initialize with setter

  /**
   * Autowired constructor.
   *
   * @param semaphoresPerPluginManager the semaphores per plugin manager
   * @param workflowExecutionDao the DAO for accessing WorkflowExecutions
   * @param workflowPostProcessor the workflow post processor
   * @param rabbitmqPublisherChannel the channel for publishing to RabbitMQ
   * @param rabbitmqConsumerChannel the channel for consuming from RabbitMQ
   * @param redissonClient the redisson client for distributed locks
   * @param dpsClient the Data Processing Service client from ECloud
   */
  public WorkflowExecutorManager(SemaphoresPerPluginManager semaphoresPerPluginManager,
      WorkflowExecutionDao workflowExecutionDao, WorkflowPostProcessor workflowPostProcessor,
      Channel rabbitmqPublisherChannel, Channel rabbitmqConsumerChannel,
      RedissonClient redissonClient, DpsClient dpsClient) {
    super(rabbitmqPublisherChannel, rabbitmqConsumerChannel, semaphoresPerPluginManager,
        workflowExecutionDao, workflowPostProcessor, redissonClient, dpsClient);
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

  public void setEcloudBaseUrl(String ecloudBaseUrl) {
    this.ecloudBaseUrl = ecloudBaseUrl;
  }

  public void setEcloudProvider(String ecloudProvider) {
    this.ecloudProvider = ecloudProvider;
  }

  public void setMetisCoreBaseUrl(String metisCoreBaseUrl) {
    this.metisCoreBaseUrl = metisCoreBaseUrl;
  }

  public void setThrottlingValues(ThrottlingValues throttlingValues){
    this.throttlingValues = throttlingValues;
  }

  public void setDpsMonitorCheckIntervalInSecs(int dpsMonitorCheckIntervalInSecs) {
    this.dpsMonitorCheckIntervalInSecs = dpsMonitorCheckIntervalInSecs;
  }

  public void setPeriodOfNoProcessedRecordsChangeInMinutes(
      int periodOfNoProcessedRecordsChangeInMinutes) {
    this.periodOfNoProcessedRecordsChangeInMinutes = periodOfNoProcessedRecordsChangeInMinutes;
  }

  @Override
  public int getDpsMonitorCheckIntervalInSecs() {
    return dpsMonitorCheckIntervalInSecs;
  }

  @Override
  public int getPeriodOfNoProcessedRecordsChangeInMinutes() {
    return periodOfNoProcessedRecordsChangeInMinutes;
  }

  @Override
  public String getEcloudBaseUrl() {
    return ecloudBaseUrl;
  }

  @Override
  public String getEcloudProvider() {
    return ecloudProvider;
  }

  @Override
  public String getMetisCoreBaseUrl() {
    return metisCoreBaseUrl;
  }

  @Override
  public ThrottlingValues getThrottlingValues() {
    return throttlingValues;
  }
}
