package metis.common.config.properties.postgres;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "postgres")
public class PostgresConfigurationProperties {

  private String server;
  private String username;
  private String password;

  public void setServer(String server) {
    this.server = server;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getServer() {
    return server;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
