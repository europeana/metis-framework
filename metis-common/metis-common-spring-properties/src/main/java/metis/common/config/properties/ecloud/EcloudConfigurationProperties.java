package metis.common.config.properties.ecloud;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "ecloud")
public class EcloudConfigurationProperties {

  private String baseUrl;
  private String dpsBaseUrl;
  private String provider;
  private String username;
  private String password;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getDpsBaseUrl() {
    return dpsBaseUrl;
  }

  public void setDpsBaseUrl(String dpsBaseUrl) {
    this.dpsBaseUrl = dpsBaseUrl;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
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
}
