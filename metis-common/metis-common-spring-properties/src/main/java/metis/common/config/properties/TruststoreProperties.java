package metis.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * Class that is used to read all configuration properties for the application.
 * <p>
 * It uses {@link PropertySource} to identify the properties on application startup
 * </p>
 */
@ConfigurationProperties(prefix = "truststore")
public class TruststoreProperties {

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
