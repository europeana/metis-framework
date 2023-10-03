package metis.common.config.properties.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "redisson")
public class RedissonConfigurationProperties {

  private int connectionPoolSize;
  private int connectTimeoutInSeconds;
  private int lockWatchdogTimeoutInSeconds;
  private int dnsMonitorIntervalInSeconds;
  private int idleConnectionTimeoutInSeconds;
  private int retryAttempts;

  public int getConnectionPoolSize() {
    return connectionPoolSize;
  }

  public void setConnectionPoolSize(int connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

  public int getConnectTimeoutInSeconds() {
    return connectTimeoutInSeconds;
  }

  public void setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
    this.connectTimeoutInSeconds = connectTimeoutInSeconds;
  }

  public int getLockWatchdogTimeoutInSeconds() {
    return lockWatchdogTimeoutInSeconds;
  }

  public void setLockWatchdogTimeoutInSeconds(int lockWatchdogTimeoutInSeconds) {
    this.lockWatchdogTimeoutInSeconds = lockWatchdogTimeoutInSeconds;
  }

  public int getDnsMonitorIntervalInSeconds() {
    return dnsMonitorIntervalInSeconds;
  }

  public void setDnsMonitorIntervalInSeconds(int dnsMonitorIntervalInSeconds) {
    this.dnsMonitorIntervalInSeconds = dnsMonitorIntervalInSeconds;
  }

  public int getIdleConnectionTimeoutInSeconds() {
    return idleConnectionTimeoutInSeconds;
  }

  public void setIdleConnectionTimeoutInSeconds(int idleConnectionTimeoutInSeconds) {
    this.idleConnectionTimeoutInSeconds = idleConnectionTimeoutInSeconds;
  }

  public int getRetryAttempts() {
    return retryAttempts;
  }

  public void setRetryAttempts(int retryAttempts) {
    this.retryAttempts = retryAttempts;
  }
}
