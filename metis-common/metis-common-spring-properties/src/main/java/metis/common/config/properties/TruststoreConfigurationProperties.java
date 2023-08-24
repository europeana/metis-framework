package metis.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "truststore")
public class TruststoreConfigurationProperties {

  private String path;
  private String password;

  public void setPath(String path) {
    this.path = path;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPath() {
    return path;
  }

  public String getPassword() {
    return password;
  }
}
