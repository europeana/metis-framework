package eu.europeana.metis.core.rest.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.ForgivingExceptionHandler;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dao.WorkflowUtils;
import eu.europeana.metis.core.execution.QueueConsumer;
import eu.europeana.metis.core.execution.SchedulerExecutor;
import eu.europeana.metis.core.execution.SemaphoresPerPluginManager;
import eu.europeana.metis.core.execution.WorkflowExecutionMonitor;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.execution.WorkflowPostProcessor;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.service.Authorizer;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import eu.europeana.metis.core.service.WorkflowExecutionFactory;
import eu.europeana.metis.exception.GenericMetisException;
import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.InternalThreadLocalMap;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Orchestrator configuration class.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-22
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@EnableScheduling
public class OrchestratorConfig implements WebMvcConfigurer {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorConfig.class);

  private static final int WAITING_TIME_FOR_THREAD_DEATH = 2;

  private final ConfigurationPropertiesHolder propertiesHolder;
  private SchedulerExecutor schedulerExecutor;
  private WorkflowExecutionMonitor workflowExecutionMonitor;
  private QueueConsumer queueConsumer;

  private Connection connection;
  private Channel publisherChannel;
  private Channel consumerChannel;
  private RedissonClient redissonClient;

  /**
   * Constructor with the required properties class.
   *
   * @param propertiesHolder the properties holder
   */
  @Autowired
  public OrchestratorConfig(ConfigurationPropertiesHolder propertiesHolder) {
    this.propertiesHolder = propertiesHolder;
  }

  @Bean
  Connection getConnection()
      throws KeyManagementException, NoSuchAlgorithmException, IOException, TimeoutException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(propertiesHolder.getRabbitmqHost());
    connectionFactory.setPort(propertiesHolder.getRabbitmqPort());
    connectionFactory.setVirtualHost(
        StringUtils.isNotBlank(propertiesHolder.getRabbitmqVirtualHost()) ? propertiesHolder
            .getRabbitmqVirtualHost() : "/");
    connectionFactory.setUsername(propertiesHolder.getRabbitmqUsername());
    connectionFactory.setPassword(propertiesHolder.getRabbitmqPassword());
    connectionFactory.setAutomaticRecoveryEnabled(true);
    if (propertiesHolder.isRabbitmqEnableSSL()) {
      connectionFactory.useSslProtocol();
    }
    //Does not close the channel if an unhandled exception occurred
    //Can happen in QueueConsumer and it's safe to not handle the execution, it will be picked up
    //again from the failsafe Executor.
    connectionFactory.setExceptionHandler(new ForgivingExceptionHandler());
    connection = connectionFactory.newConnection();
    return connection;
  }

  @Bean(name = "rabbitmqPublisherChannel")
  Channel getRabbitmqPublisherChannel(Connection connection) throws IOException {
    publisherChannel = connection.createChannel();
    setupChannelProperties(publisherChannel);
    return publisherChannel;
  }

  @Bean(name = "rabbitmqConsumerChannel")
  Channel getRabbitmqConsumerChannel(Connection connection) throws IOException {
    consumerChannel = connection.createChannel();
    setupChannelProperties(consumerChannel);
    return consumerChannel;
  }

  private void setupChannelProperties(Channel channel) throws IOException {
    Map<String, Object> args = new ConcurrentHashMap<>();
    args.put("x-max-priority",
        propertiesHolder.getRabbitmqHighestPriority());//Higher number means higher priority
    //Second boolean durable to false
    channel.queueDeclare(propertiesHolder.getRabbitmqQueueName(), false, false, false, args);
  }

  @Bean
  RedissonClient getRedissonClient() throws MalformedURLException {
    Config config = new Config();

    SingleServerConfig singleServerConfig;
    if (propertiesHolder.isRedisEnableSSL()) {
      singleServerConfig = config.useSingleServer().setAddress(String
          .format("rediss://%s:%s", propertiesHolder.getRedisHost(),
              propertiesHolder.getRedisPort()));
      if (propertiesHolder.isRedisEnableCustomTruststore()) {
        singleServerConfig
            .setSslTruststore(new File(propertiesHolder.getTruststorePath()).toURI().toURL());
        singleServerConfig.setSslTruststorePassword(propertiesHolder.getTruststorePassword());
      }
    } else {
      singleServerConfig = config.useSingleServer().setAddress(String
          .format("redis://%s:%s", propertiesHolder.getRedisHost(),
              propertiesHolder.getRedisPort()));
    }
    if (StringUtils.isNotEmpty(propertiesHolder.getRedisPassword())) {
      singleServerConfig.setPassword(propertiesHolder.getRedisPassword());
    }
    config.setLockWatchdogTimeout(TimeUnit.SECONDS.toMillis(propertiesHolder
        .getRedissonLockWatchdogTimeoutInSecs())); //Give some secs to unlock if connection lost, or if too long to unlock
    redissonClient = Redisson.create(config);
    return redissonClient;
  }

  @Bean
  public OrchestratorService getOrchestratorService(WorkflowDao workflowDao,
      WorkflowExecutionDao workflowExecutionDao, WorkflowUtils workflowUtils, DatasetDao datasetDao,
      WorkflowExecutionFactory workflowExecutionFactory,
      WorkflowExecutorManager workflowExecutorManager, Authorizer authorizer,
      DepublishRecordIdDao depublishRecordIdDao) {
    OrchestratorService orchestratorService = new OrchestratorService(workflowExecutionFactory,
        workflowDao, workflowExecutionDao, workflowUtils, datasetDao, workflowExecutorManager,
        redissonClient, authorizer, depublishRecordIdDao);
    orchestratorService.setSolrCommitPeriodInMins(propertiesHolder.getSolrCommitPeriodInMins());
    return orchestratorService;
  }

  @Bean
  public WorkflowExecutionFactory getWorkflowExecutionFactory(
      WorkflowExecutionDao workflowExecutionDao, WorkflowUtils workflowUtils,
      DatasetXsltDao datasetXsltDao, DepublishRecordIdDao depublishRecordIdDao) {
    WorkflowExecutionFactory workflowExecutionFactory = new WorkflowExecutionFactory(datasetXsltDao,
        depublishRecordIdDao, workflowExecutionDao, workflowUtils);
    workflowExecutionFactory
        .setValidationExternalProperties(propertiesHolder.getValidationExternalProperties());
    workflowExecutionFactory
        .setValidationInternalProperties(propertiesHolder.getValidationInternalProperties());
    workflowExecutionFactory.setMetisUseAlternativeIndexingEnvironment(
        propertiesHolder.isMetisUseAlternativeIndexingEnvironment());
    workflowExecutionFactory.setDefaultSamplingSizeForLinkChecking(
        propertiesHolder.getMetisLinkCheckingDefaultSamplingSize());
    return workflowExecutionFactory;
  }

  @Bean
  public ScheduleWorkflowService getScheduleWorkflowService(
      ScheduledWorkflowDao scheduledWorkflowDao, WorkflowDao workflowDao, DatasetDao datasetDao,
      Authorizer authorizer) {
    return new ScheduleWorkflowService(scheduledWorkflowDao, workflowDao, datasetDao, authorizer);
  }

  @Bean
  public ProxiesService getProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient, DpsClient dpsClient, Authorizer authorizer) {
    return new ProxiesService(workflowExecutionDao, ecloudDataSetServiceClient, recordServiceClient,
        fileServiceClient, dpsClient, propertiesHolder.getEcloudProvider(), authorizer);
  }

  @Bean
  public WorkflowPostProcessor workflowPostProcessor(DepublishRecordIdDao depublishRecordIdDao,
      DatasetDao datasetDao, WorkflowExecutionDao workflowExecutionDao, DpsClient dpsClient) {
    return new WorkflowPostProcessor(depublishRecordIdDao, datasetDao, workflowExecutionDao,
        dpsClient);
  }

  @Bean
  public SemaphoresPerPluginManager semaphoresPerPluginManager() {
    return new SemaphoresPerPluginManager(propertiesHolder.getMaxConcurrentThreads());
  }

  @Bean
  public WorkflowExecutorManager getWorkflowExecutorManager(
      SemaphoresPerPluginManager semaphoresPerPluginManager,
      WorkflowExecutionDao workflowExecutionDao, WorkflowPostProcessor workflowPostProcessor,
      @Qualifier("rabbitmqPublisherChannel") Channel rabbitmqPublisherChannel,
      @Qualifier("rabbitmqConsumerChannel") Channel rabbitmqConsumerChannel,
      RedissonClient redissonClient, DpsClient dpsClient) {
    WorkflowExecutorManager workflowExecutorManager = new WorkflowExecutorManager(
        semaphoresPerPluginManager, workflowExecutionDao, workflowPostProcessor,
        rabbitmqPublisherChannel, rabbitmqConsumerChannel, redissonClient, dpsClient);
    workflowExecutorManager.setRabbitmqQueueName(propertiesHolder.getRabbitmqQueueName());
    workflowExecutorManager
        .setDpsMonitorCheckIntervalInSecs(propertiesHolder.getDpsMonitorCheckIntervalInSecs());
    workflowExecutorManager.setPeriodOfNoProcessedRecordsChangeInMinutes(
        propertiesHolder.getPeriodOfNoProcessedRecordsChangeInMinutes());
    workflowExecutorManager.setEcloudBaseUrl(propertiesHolder.getEcloudBaseUrl());
    workflowExecutorManager.setEcloudProvider(propertiesHolder.getEcloudProvider());
    workflowExecutorManager.setMetisCoreBaseUrl(propertiesHolder.getMetisCoreBaseUrl());
    return workflowExecutorManager;
  }

  @Bean
  public WorkflowExecutionDao getWorkflowExecutionDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider) {
    WorkflowExecutionDao workflowExecutionDao = new WorkflowExecutionDao(morphiaDatastoreProvider);
    workflowExecutionDao
        .setWorkflowExecutionsPerRequest(RequestLimits.WORKFLOW_EXECUTIONS_PER_REQUEST.getLimit());
    workflowExecutionDao
        .setMaxServedExecutionListLength(propertiesHolder.getMaxServedExecutionListLength());
    return workflowExecutionDao;
  }

  @Bean
  WorkflowUtils getWorkflowUtils(WorkflowExecutionDao workflowExecutionDao,
      DepublishRecordIdDao depublishRecordIdDao) {
    return new WorkflowUtils(workflowExecutionDao, depublishRecordIdDao);
  }

  @Bean
  public ScheduledWorkflowDao getScheduledWorkflowDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider) {
    return new ScheduledWorkflowDao(morphiaDatastoreProvider);
  }

  @Bean
  public WorkflowDao getWorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    return new WorkflowDao(morphiaDatastoreProvider);
  }

  @Bean
  public WorkflowExecutionMonitor getWorkflowExecutionMonitor(
      WorkflowExecutorManager workflowExecutorManager, WorkflowExecutionDao workflowExecutionDao,
      RedissonClient redissonClient) {

    // Computes the leniency for the failsafe action: how long ago (worst case) can the last update
    // time have been set before we assume the execution hangs.
    final Duration failsafeLeniency = Duration.ZERO
        .plusMillis(propertiesHolder.getDpsConnectTimeoutInMillisecs())
        .plusMillis(propertiesHolder.getDpsReadTimeoutInMillisecs())
        .plusSeconds(propertiesHolder.getDpsMonitorCheckIntervalInSecs())
        .plusSeconds(propertiesHolder.getFailsafeMarginOfInactivityInSecs());

    // Create and return the workflow execution monitor.
    workflowExecutionMonitor = new WorkflowExecutionMonitor(workflowExecutorManager,
        workflowExecutionDao, redissonClient, failsafeLeniency);
    return workflowExecutionMonitor;
  }

  @Bean
  public SchedulerExecutor getSchedulingExecutor(OrchestratorService orchestratorService,
      ScheduleWorkflowService scheduleWorkflowService, RedissonClient redissonClient) {
    schedulerExecutor = new SchedulerExecutor(orchestratorService, scheduleWorkflowService,
        redissonClient);
    return schedulerExecutor;
  }

  @Bean
  public QueueConsumer getQueueConsumer(WorkflowExecutorManager workflowExecutionManager,
      WorkflowExecutionMonitor workflowExecutionMonitor,
      @Qualifier("rabbitmqConsumerChannel") Channel rabbitmqConsumerChannel) throws IOException {
    queueConsumer = new QueueConsumer(rabbitmqConsumerChannel,
        propertiesHolder.getRabbitmqQueueName(), workflowExecutionManager, workflowExecutionManager,
        workflowExecutionMonitor);
    return queueConsumer;
  }

  @Scheduled(fixedDelayString = "${periodic.failsafe.check.in.millisecs}")
  public void runFailsafeExecutor() {
    LOGGER.info("Failsafe task started (runs every {} milliseconds).",
        propertiesHolder.getPeriodicFailsafeCheckInMillisecs());
    this.workflowExecutionMonitor.performFailsafe();
    LOGGER.info("Failsafe task finished.");
  }

  @Scheduled(fixedDelayString = "${periodic.scheduler.check.in.millisecs}", initialDelayString = "${periodic.scheduler.check.in.millisecs}")
  public void runSchedulingExecutor() {
    LOGGER.info("Scheduler task started (runs every {} milliseconds).",
        propertiesHolder.getPeriodicSchedulerCheckInMillisecs());
    this.schedulerExecutor.performScheduling();
    LOGGER.info("Scheduler task finished.");
  }

  @Scheduled(fixedDelayString = "${polling.timeout.for.cleaning.completion.service.in.millisecs}", initialDelayString = "${polling.timeout.for.cleaning.completion.service.in.millisecs}")
  public void runQueueConsumerCleanup() throws InterruptedException {
    LOGGER.debug("Queue consumer cleanup started (runs every {} milliseconds).",
        propertiesHolder.getPollingTimeoutForCleaningCompletionServiceInMillisecs());
    this.queueConsumer.checkAndCleanCompletionService();
    LOGGER.debug("Queue consumer cleanup finished.");
  }

  @PreDestroy
  public void close() throws GenericMetisException {
    try {
      // Shut down RabbitMQ
      if (publisherChannel != null && publisherChannel.isOpen()) {
        publisherChannel.close();
      }
      if (consumerChannel != null && consumerChannel.isOpen()) {
        consumerChannel.close();
      }
      if (connection != null && connection.isOpen()) {
        connection.close();
      }
      // Shutdown the queue consumer
      if (queueConsumer != null) {
        queueConsumer.close();
      }

      // Shut down Redisson
      if (redissonClient != null && !redissonClient.isShuttingDown()) {
        redissonClient.shutdown();
      }
      FastThreadLocal.removeAll();
      FastThreadLocal.destroy();
      InternalThreadLocalMap.remove();
      InternalThreadLocalMap.destroy();
      ThreadDeathWatcher.awaitInactivity(WAITING_TIME_FOR_THREAD_DEATH, TimeUnit.SECONDS);
    } catch (IOException | TimeoutException e) {
      throw new GenericMetisException("Could not shutdown resources properly.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GenericMetisException("Could not shutdown resources properly.", e);
    }
  }
}
