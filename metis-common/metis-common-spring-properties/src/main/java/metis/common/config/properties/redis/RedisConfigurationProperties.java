package metis.common.config.properties.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "redis")
public class RedisConfigurationProperties {

  private String host;
  private String port;
  private String username;
  private String password;
  private boolean enableSsl;
  private boolean enableCustomTruststore;

  @NestedConfigurationProperty
  //Keep the name as is(zookeeper) for the spring mapping.
  private RedissonConfigurationProperties redisson;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
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

  public RedissonConfigurationProperties getRedisson() {
    return redisson;
  }

  public void setRedisson(RedissonConfigurationProperties redisson) {
    this.redisson = redisson;
  }
}
