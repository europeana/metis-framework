package eu.europeana.metis.authentication.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "metis-authentication")
public class MetisAuthenticationConfigurationProperties {

  private int accessTokenExpireTimeInMinutes;
  private String allowedCorsHosts;

  public int getAccessTokenExpireTimeInMinutes() {
    return accessTokenExpireTimeInMinutes;
  }

  public void setAccessTokenExpireTimeInMinutes(int accessTokenExpireTimeInMinutes) {
    this.accessTokenExpireTimeInMinutes = accessTokenExpireTimeInMinutes;
  }

  public String getAllowedCorsHosts() {
    return allowedCorsHosts;
  }

  public void setAllowedCorsHosts(String allowedCorsHosts) {
    this.allowedCorsHosts = allowedCorsHosts;
  }
}
