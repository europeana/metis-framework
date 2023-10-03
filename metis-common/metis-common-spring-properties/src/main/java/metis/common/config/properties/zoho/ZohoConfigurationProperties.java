package metis.common.config.properties.zoho;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "zoho")
public class ZohoConfigurationProperties {

  private String initialGrantToken;
  private String refreshToken;
  private String currentUserEmail;
  private String clientId;
  private String clientSecret;
  private String redirectUri;

  public String getInitialGrantToken() {
    return initialGrantToken;
  }

  public void setInitialGrantToken(String initialGrantToken) {
    this.initialGrantToken = initialGrantToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getCurrentUserEmail() {
    return currentUserEmail;
  }

  public void setCurrentUserEmail(String currentUserEmail) {
    this.currentUserEmail = currentUserEmail;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }
}
