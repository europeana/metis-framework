package eu.europeana.metis.core.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import eu.europeana.metis.core.workflow.ValidationProperties;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-03-13
 */
@Component
@PropertySource({"classpath:metis.properties"})
public class ConfigurationPropertiesHolder {

  //Orchestration
  @Value("${max.concurrent.threads}")
  private int maxConcurrentThreads;
  @Value("${dps.monitor.check.interval.in.secs}")
  private int dpsMonitorCheckIntervalInSecs;
  @Value("${dps.connect.timeout.in.millisecs}")
  private int dpsConnectTimeoutInMillisecs;
  @Value("${dps.read.timeout.in.millisecs}")
  private int dpsReadTimeoutInMillisecs;
  @Value("${failsafe.margin.of.inactivity.in.secs}")
  private int failsafeMarginOfInactivityInSecs;
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
  @Value("${redis.enableSSL}")
  private boolean redisEnableSSL;
  @Value("${redis.enable.custom.truststore}")
  private boolean redisEnableCustomTruststore;
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
  @Value("${rabbitmq.virtual.host}")
  private String rabbitmqVirtualHost;
  @Value("${rabbitmq.queue.name}")
  private String rabbitmqQueueName;
  @Value("${rabbitmq.highest.priority}")
  private int rabbitmqHighestPriority;
  @Value("${rabbitmq.enableSSL}")
  private boolean rabbitmqEnableSSL;

  @Value("${metis.core.baseUrl}")
  private String metisCoreBaseUrl;

  @Value("${metis.use.alternative.indexing.environment}")
  private boolean metisUseAlternativeIndexingEnvironment;

  @Value("${solr.commit.period.in.mins}")
  private int solrCommitPeriodInMins;

  @Value("${socks.proxy.enabled}")
  private boolean socksProxyEnabled;
  @Value("${socks.proxy.host}")
  private String socksProxyHost;
  @Value("${socks.proxy.port}")
  private String socksProxyPort;
  @Value("${socks.proxy.username}")
  private String socksProxyUsername;
  @Value("${socks.proxy.password}")
  private String socksProxyPassword;

  //Custom trustore
  @Value("${truststore.path}")
  private String truststorePath;
  @Value("${truststore.password}")
  private String truststorePassword;

  //Mongo
  @Value("${mongo.hosts}")
  private String[] mongoHosts;
  @Value("${mongo.port}")
  private int[] mongoPorts;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.authentication.db}")
  private String mongoAuthenticationDb;
  @Value("${mongo.db}")
  private String mongoDb;
  @Value("${mongo.enableSSL}")
  private boolean mongoEnableSSL;

  //Validation
  @Value("${validation.internal.schema.zip}")
  private String validationInternalSchemaZip;
  @Value("${validation.internal.schema.root}")
  private String validationInternalSchemaRoot;
  @Value("${validation.internal.schematron.root}")
  private String validationInternalSchematronRoot;
  @Value("${validation.external.schema.zip}")
  private String validationExternalSchemaZip;
  @Value("${validation.external.schema.root}")
  private String validationExternalSchemaRoot;
  @Value("${validation.external.schematron.root}")
  private String validationExternalSchematronRoot;

  //Authentication
  @Value("${authentication.baseUrl}")
  private String authenticationBaseUrl;

  @Value("${allowed.cors.hosts}")
  private String[] allowedCorsHosts;

  @Value("${ecloud.baseUrl}")
  private String ecloudBaseUrl;
  @Value("${ecloud.dps.baseUrl}")
  private String ecloudDpsBaseUrl;
  @Value("${ecloud.provider}")
  private String ecloudProvider;
  @Value("${ecloud.username}")
  private String ecloudUsername;
  @Value("${ecloud.password}")
  private String ecloudPassword;

  public int getMaxConcurrentThreads() {
    return maxConcurrentThreads;
  }

  public int getDpsMonitorCheckIntervalInSecs() {
    return dpsMonitorCheckIntervalInSecs;
  }

  public int getDpsConnectTimeoutInMillisecs() {
    return dpsConnectTimeoutInMillisecs;
  }

  public int getDpsReadTimeoutInMillisecs() {
    return dpsReadTimeoutInMillisecs;
  }

  public int getFailsafeMarginOfInactivityInSecs() {
    return failsafeMarginOfInactivityInSecs;
  }

  public int getPeriodicFailsafeCheckInMillisecs() {
    return periodicFailsafeCheckInMillisecs;
  }

  public int getPeriodicSchedulerCheckInMillisecs() {
    return periodicSchedulerCheckInMillisecs;
  }

  public int getPollingTimeoutForCleaningCompletionServiceInSecs() {
    return pollingTimeoutForCleaningCompletionServiceInSecs;
  }

  public String getRedisHost() {
    return redisHost;
  }

  public int getRedisPort() {
    return redisPort;
  }

  public String getRedisPassword() {
    return redisPassword;
  }

  public boolean isRedisEnableSSL() {
    return redisEnableSSL;
  }

  public boolean isRedisEnableCustomTruststore() {
    return redisEnableCustomTruststore;
  }

  public int getRedissonLockWatchdogTimeoutInSecs() {
    return redissonLockWatchdogTimeoutInSecs;
  }

  public String getRabbitmqHost() {
    return rabbitmqHost;
  }

  public int getRabbitmqPort() {
    return rabbitmqPort;
  }

  public String getRabbitmqUsername() {
    return rabbitmqUsername;
  }

  public String getRabbitmqPassword() {
    return rabbitmqPassword;
  }

  public String getRabbitmqVirtualHost() {
    return rabbitmqVirtualHost;
  }

  public boolean isRabbitmqEnableSSL() {
    return rabbitmqEnableSSL;
  }

  public String getRabbitmqQueueName() {
    return rabbitmqQueueName;
  }

  public int getRabbitmqHighestPriority() {
    return rabbitmqHighestPriority;
  }

  public String getMetisCoreBaseUrl() {
    return metisCoreBaseUrl;
  }

  public boolean getMetisUseAlternativeIndexingEnvironment() {
    return metisUseAlternativeIndexingEnvironment;
  }

  public int getSolrCommitPeriodInMins() {
    return solrCommitPeriodInMins;
  }

  public boolean isSocksProxyEnabled() {
    return socksProxyEnabled;
  }

  public String getSocksProxyHost() {
    return socksProxyHost;
  }

  public String getSocksProxyPort() {
    return socksProxyPort;
  }

  public String getSocksProxyUsername() {
    return socksProxyUsername;
  }

  public String getSocksProxyPassword() {
    return socksProxyPassword;
  }

  public String getTruststorePath() {
    return truststorePath;
  }

  public String getTruststorePassword() {
    return truststorePassword;
  }

  public String[] getMongoHosts() {
    return mongoHosts == null ? null : mongoHosts.clone();
  }

  public int[] getMongoPorts() {
    return mongoPorts == null ? null : mongoPorts.clone();
  }

  public String getMongoUsername() {
    return mongoUsername;
  }

  public String getMongoPassword() {
    return mongoPassword;
  }

  public String getMongoAuthenticationDb() {
    return mongoAuthenticationDb;
  }

  public String getMongoDb() {
    return mongoDb;
  }

  public boolean isMongoEnableSSL() {
    return mongoEnableSSL;
  }

  public String getAuthenticationBaseUrl() {
    return authenticationBaseUrl;
  }

  public String[] getAllowedCorsHosts() {
    return allowedCorsHosts == null ? null : allowedCorsHosts.clone();
  }

  public String getEcloudBaseUrl() {
    return ecloudBaseUrl;
  }

  public String getEcloudDpsBaseUrl() {
    return ecloudDpsBaseUrl;
  }

  public String getEcloudProvider() {
    return ecloudProvider;
  }

  public String getEcloudUsername() {
    return ecloudUsername;
  }

  public String getEcloudPassword() {
    return ecloudPassword;
  }

  public ValidationProperties getValidationExternalProperties() {
    return new ValidationProperties(validationExternalSchemaZip, validationExternalSchemaRoot,
        validationExternalSchematronRoot);
  }

  public ValidationProperties getValidationInternalProperties() {
    return new ValidationProperties(validationInternalSchemaZip, validationInternalSchemaRoot,
        validationInternalSchematronRoot);
  }
}
