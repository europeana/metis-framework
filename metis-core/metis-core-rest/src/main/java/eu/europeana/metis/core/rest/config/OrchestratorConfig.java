package eu.europeana.metis.core.rest.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.metis.core.dao.DatasetDao;
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
import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.InternalThreadLocalMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-22
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.core.rest"})
@PropertySource({"classpath:metis.properties"})
@EnableScheduling
public class OrchestratorConfig extends WebMvcConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorConfig.class);

  //Orchestration
  @Value("${max.concurrent.threads}")
  private int maxConcurrentThreads;
  @Value("${monitor.check.interval.in.secs}")
  private int monitorCheckIntervalInSecs;
  @Value("${periodic.failsafe.check.in.millisecs}")
  private int periodicFailsafeCheckInMillisecs;
  @Value("${periodic.scheduler.check.in.millisecs}")
  private int periodicSchedulerCheckInMillisecs;
  @Value("${polling.timeout.for.cleaning.completion.service.in.secs}")
  private int pollingTimeoutForCleaningCompletionServiceInSecs;

  //Redis
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password}")
  private String redisPassword;
  @Value("${redisson.lock.watchdog.timeout.in.secs}")
  private int redissonLockWatchdogTimeoutInSecs;

  //RabbitMq
  @Value("${rabbitmq.host}")
  private String rabbitmqHost;
  @Value("${rabbitmq.port}")
  private int rabbitmqPort;
  @Value("${rabbitmq.username}")
  private String rabbitmqUsername;
  @Value("${rabbitmq.password}")
  private String rabbitmqPassword;
  @Value("${rabbitmq.queue.name}")
  private String rabbitmqQueueName;
  @Value("${rabbitmq.highest.priority}")
  private int rabbitmqHighestPriority;

  @Value("${ecloud.provider}")
  private String ecloudProvider;
  @Value("${ecloud.baseUrl}")
  private String ecloudBaseUrl;

  private Connection connection;
  private Channel channel;
  private RedissonClient redissonClient;

  @Autowired
  private SchedulerExecutor schedulerExecutor;
  @Autowired
  private FailsafeExecutor failsafeExecutor;

  @Bean
  Channel getRabbitmqChannel() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitmqHost);
    factory.setPort(rabbitmqPort);
    factory.setUsername(rabbitmqUsername);
    factory.setPassword(rabbitmqPassword);
    factory.setAutomaticRecoveryEnabled(true);
    connection = factory.newConnection();
    channel = connection.createChannel();
    Map<String, Object> args = new HashMap<>();
    args.put("x-max-priority", rabbitmqHighestPriority);//Higher number means higher priority
    //Second boolean durable to false
    channel.queueDeclare(rabbitmqQueueName, false, false, false, args);
    return channel;
  }

  @Bean
  RedissonClient getRedissonClient() {
    Config config = new Config();
    SingleServerConfig singleServerConfig = config.useSingleServer()
        .setAddress(String.format("redis://%s:%s", redisHost, redisPort));
    if (StringUtils.isNotEmpty(redisPassword)) {
      singleServerConfig.setPassword(redisPassword);
    }
    config.setLockWatchdogTimeout(TimeUnit.SECONDS.toMillis(
        redissonLockWatchdogTimeoutInSecs)); //Give some secs to unlock if connection lost, or if too long to unlock
    redissonClient = Redisson.create(config);
    return redissonClient;
  }

  @Bean
  public OrchestratorService getOrchestratorService(WorkflowDao workflowDao,
      WorkflowExecutionDao workflowExecutionDao,
      ScheduledWorkflowDao scheduledWorkflowDao,
      DatasetDao datasetDao,
      WorkflowExecutorManager workflowExecutorManager,
      DataSetServiceClient ecloudDataSetServiceClient) throws IOException {
    OrchestratorService orchestratorService = new OrchestratorService(workflowDao,
        workflowExecutionDao,
        scheduledWorkflowDao, datasetDao, workflowExecutorManager, ecloudDataSetServiceClient,
        redissonClient);
    orchestratorService.setEcloudProvider(ecloudProvider);
    return orchestratorService;
  }

  @Bean
  public ProxiesService getProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient,
      DpsClient dpsClient) {
    return new ProxiesService(workflowExecutionDao, ecloudDataSetServiceClient, recordServiceClient,
        fileServiceClient, dpsClient, ecloudProvider);
  }

  @Bean
  public WorkflowExecutorManager getWorkflowExecutorManager(
      WorkflowExecutionDao workflowExecutionDao, Channel rabbitmqChannel,
      RedissonClient redissonClient, DpsClient dpsClient) {
    WorkflowExecutorManager workflowExecutorManager = new WorkflowExecutorManager(
        workflowExecutionDao, rabbitmqChannel, redissonClient, dpsClient);
    workflowExecutorManager.setRabbitmqQueueName(rabbitmqQueueName);
    workflowExecutorManager.setMaxConcurrentThreads(maxConcurrentThreads);
    workflowExecutorManager.setMonitorCheckIntervalInSecs(monitorCheckIntervalInSecs);
    workflowExecutorManager.setPollingTimeoutForCleaningCompletionServiceInSecs(
        pollingTimeoutForCleaningCompletionServiceInSecs);
    workflowExecutorManager.setEcloudBaseUrl(ecloudBaseUrl);
    workflowExecutorManager.setEcloudProvider(ecloudProvider);
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
    return new FailsafeExecutor(orchestratorService, redissonClient);
  }

  @Bean
  public SchedulerExecutor getSchedulingExecutor(OrchestratorService orchestratorService,
      RedissonClient redissonClient) {
    return new SchedulerExecutor(orchestratorService, redissonClient);
  }

  @Scheduled(fixedDelayString = "${periodic.failsafe.check.in.millisecs}",
      initialDelayString = "${periodic.failsafe.check.in.millisecs}")
  public void runFailsafeExecutor() {
    LOGGER.info("Failsafe task started (runs every {} milliseconds).",
        periodicFailsafeCheckInMillisecs);
    this.failsafeExecutor.performFailsafe();
    LOGGER.info("Failsafe task finished.");
  }

  @Scheduled(fixedDelayString = "${periodic.scheduler.check.in.millisecs}",
      initialDelayString = "${periodic.scheduler.check.in.millisecs}")
  public void runSchedulingExecutor() {
    LOGGER.info("Scheduler task started (runs every {} milliseconds).",
        periodicSchedulerCheckInMillisecs);
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
