package metis.common.config.properties.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitmqConfigurationProperties {

  private String host;
  private int port;
  private String username;
  private String password;
  private String virtualHost;
  private String queueName;
  private int highestPriority;
  private boolean enableSsl;
  private boolean enableCustomTruststore;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getVirtualHost() {
    return virtualHost;
  }

  public void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public int getHighestPriority() {
    return highestPriority;
  }

  public void setHighestPriority(int highestPriority) {
    this.highestPriority = highestPriority;
  }

  public boolean isEnableSsl() {
    return enableSsl;
  }

  public void setEnableSsl(boolean enableSsl) {
    this.enableSsl = enableSsl;
  }

  public boolean isEnableCustomTruststore() {
    return enableCustomTruststore;
  }

  public void setEnableCustomTruststore(boolean enableCustomTruststore) {
    this.enableCustomTruststore = enableCustomTruststore;
  }
}
