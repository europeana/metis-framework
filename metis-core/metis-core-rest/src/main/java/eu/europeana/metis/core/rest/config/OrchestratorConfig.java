package eu.europeana.metis.core.rest.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.execution.FailsafeExecutor;
import eu.europeana.metis.core.execution.SchedulerExecutor;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.InternalThreadLocalMap;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-22
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@EnableScheduling
public class OrchestratorConfig extends WebMvcConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorConfig.class);

  private ConfigurationPropertiesHolder propertiesHolder;
  private SchedulerExecutor schedulerExecutor;
  private FailsafeExecutor failsafeExecutor;

  private Connection connection;
  private Channel channel;
  private RedissonClient redissonClient;

  @Autowired
  public OrchestratorConfig(ConfigurationPropertiesHolder propertiesHolder) {
    this.propertiesHolder = propertiesHolder;
  }

  @Bean
  Channel getRabbitmqChannel()
      throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(propertiesHolder.getRabbitmqHost());
    factory.setPort(propertiesHolder.getRabbitmqPort());
    factory.setVirtualHost(
        StringUtils.isNotBlank(propertiesHolder.getRabbitmqVirtualHost()) ? propertiesHolder
            .getRabbitmqVirtualHost() : "/");
    factory.setUsername(propertiesHolder.getRabbitmqUsername());
    factory.setPassword(propertiesHolder.getRabbitmqPassword());
    factory.setAutomaticRecoveryEnabled(true);
    if (propertiesHolder.isRabbitmqEnableSSL()) {
      factory.useSslProtocol();
    }
    connection = factory.newConnection();
    channel = connection.createChannel();
    Map<String, Object> args = new HashMap<>();
    args.put("x-max-priority",
        propertiesHolder.getRabbitmqHighestPriority());//Higher number means higher priority
    //Second boolean durable to false
    channel.queueDeclare(propertiesHolder.getRabbitmqQueueName(), false, false, false, args);
    return channel;
  }

  @Bean
  RedissonClient getRedissonClient() {
    Config config = new Config();

    SingleServerConfig singleServerConfig;
    if (propertiesHolder.isRedisEnableSSL()) {
      singleServerConfig = config.useSingleServer()
          .setAddress(
              String.format("rediss://%s:%s", propertiesHolder.getRedisHost(), propertiesHolder
                  .getRedisPort()))
          .setSslTruststore(new File(propertiesHolder.getTruststorePath()).toURI())
          .setSslTruststorePassword(propertiesHolder.getTruststorePassword());
    } else {
      singleServerConfig = config.useSingleServer()
          .setAddress(
              String.format("redis://%s:%s", propertiesHolder.getRedisHost(), propertiesHolder
                  .getRedisPort()));
    }
    if (StringUtils.isNotEmpty(propertiesHolder.getRedisPassword())) {
      singleServerConfig.setPassword(propertiesHolder.getRedisPassword());
    }
    config.setLockWatchdogTimeout(TimeUnit.SECONDS.toMillis(
        propertiesHolder
            .getRedissonLockWatchdogTimeoutInSecs())); //Give some secs to unlock if connection lost, or if too long to unlock
    redissonClient = Redisson.create(config);
    return redissonClient;
  }

  @Bean
  public OrchestratorService getOrchestratorService(WorkflowDao workflowDao,
      WorkflowExecutionDao workflowExecutionDao,
      DatasetDao datasetDao, DatasetXsltDao datasetXsltDao,
      WorkflowExecutorManager workflowExecutorManager) throws IOException {
    OrchestratorService orchestratorService = new OrchestratorService(workflowDao,
        workflowExecutionDao, datasetDao, datasetXsltDao, workflowExecutorManager,
        redissonClient);
    orchestratorService.setMetisCoreUrl(propertiesHolder.getMetisCoreBaseUrl());
    return orchestratorService;
  }

  @Bean
  public ScheduleWorkflowService getScheduleWorkflowService(
      ScheduledWorkflowDao scheduledWorkflowDao, WorkflowDao workflowDao, DatasetDao datasetDao) {
    return new ScheduleWorkflowService(scheduledWorkflowDao, workflowDao, datasetDao);
  }

  @Bean
  public ProxiesService getProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient,
      DpsClient dpsClient) {
    return new ProxiesService(workflowExecutionDao, ecloudDataSetServiceClient, recordServiceClient,
        fileServiceClient, dpsClient, propertiesHolder.getEcloudProvider());
  }

  @Bean
  public WorkflowExecutorManager getWorkflowExecutorManager(
      WorkflowExecutionDao workflowExecutionDao, Channel rabbitmqChannel,
      RedissonClient redissonClient, DpsClient dpsClient) {
    WorkflowExecutorManager workflowExecutorManager = new WorkflowExecutorManager(
        workflowExecutionDao, rabbitmqChannel, redissonClient, dpsClient);
    workflowExecutorManager.setRabbitmqQueueName(propertiesHolder.getRabbitmqQueueName());
    workflowExecutorManager.setMaxConcurrentThreads(propertiesHolder.getMaxConcurrentThreads());
    workflowExecutorManager
        .setMonitorCheckIntervalInSecs(propertiesHolder.getMonitorCheckIntervalInSecs());
    workflowExecutorManager.setPollingTimeoutForCleaningCompletionServiceInSecs(
        propertiesHolder.getPollingTimeoutForCleaningCompletionServiceInSecs());
    workflowExecutorManager.setEcloudBaseUrl(propertiesHolder.getEcloudBaseUrl());
    workflowExecutorManager.setEcloudProvider(propertiesHolder.getEcloudProvider());
    return workflowExecutorManager;
  }

  @Bean
  public WorkflowExecutionDao getWorkflowExecutionDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider) {
    WorkflowExecutionDao workflowExecutionDao = new WorkflowExecutionDao(
        morphiaDatastoreProvider);
    workflowExecutionDao.setWorkflowExecutionsPerRequest(
        RequestLimits.WORKFLOW_EXECUTIONS_PER_REQUEST.getLimit());
    return workflowExecutionDao;
  }

  @Bean
  public ScheduledWorkflowDao getScheduledWorkflowDao(
      MorphiaDatastoreProvider morphiaDatastoreProvider) {
    return new ScheduledWorkflowDao(morphiaDatastoreProvider);
  }

  @Bean
  public WorkflowDao getWorkflowDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    WorkflowDao workflowDao = new WorkflowDao(morphiaDatastoreProvider);
    workflowDao.setWorkflowsPerRequest(RequestLimits.WORKFLOWS_PER_REQUEST.getLimit());
    return workflowDao;
  }

  @Bean
  public FailsafeExecutor getFailsafeExecutor(OrchestratorService orchestratorService,
      RedissonClient redissonClient) {
    failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient);
    return failsafeExecutor;
  }

  @Bean
  public SchedulerExecutor getSchedulingExecutor(OrchestratorService orchestratorService,
      ScheduleWorkflowService scheduleWorkflowService,
      RedissonClient redissonClient) {
    schedulerExecutor = new SchedulerExecutor(orchestratorService, scheduleWorkflowService,
        redissonClient);
    return schedulerExecutor;
  }

  @Scheduled(fixedDelayString = "${periodic.failsafe.check.in.millisecs}",
      initialDelayString = "${periodic.failsafe.check.in.millisecs}")
  public void runFailsafeExecutor() {
    LOGGER.info("Failsafe task started (runs every {} milliseconds).",
        propertiesHolder.getPeriodicFailsafeCheckInMillisecs());
    this.failsafeExecutor.performFailsafe();
    LOGGER.info("Failsafe task finished.");
  }

  @Scheduled(fixedDelayString = "${periodic.scheduler.check.in.millisecs}",
      initialDelayString = "${periodic.scheduler.check.in.millisecs}")
  public void runSchedulingExecutor() {
    LOGGER.info("Scheduler task started (runs every {} milliseconds).",
        propertiesHolder.getPeriodicSchedulerCheckInMillisecs());
    this.schedulerExecutor.performScheduling();
    LOGGER.info("Scheduler task finished.");
  }

  @PreDestroy
  public void close()
      throws IOException, TimeoutException, InterruptedException {

    // Shut down RabbitMQ
    if (channel != null && channel.isOpen()) {
      channel.close();
    }
    if (connection != null && connection.isOpen()) {
      connection.close();
    }

    // Shut down Redisson
    if (redissonClient != null && !redissonClient.isShuttingDown()) {
      redissonClient.shutdown();
    }
    FastThreadLocal.removeAll();
    FastThreadLocal.destroy();
    InternalThreadLocalMap.remove();
    InternalThreadLocalMap.destroy();
    ThreadDeathWatcher.awaitInactivity(2, TimeUnit.SECONDS);
  }
}
