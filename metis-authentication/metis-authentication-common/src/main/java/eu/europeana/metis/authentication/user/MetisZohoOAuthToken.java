package eu.europeana.metis.authentication.user;

import com.zoho.oauth.contract.ZohoOAuthTokens;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The token information for the Zoho service.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-20
 */
@Entity
@Table(name = "metis_zoho_oauth_tokens")
public class MetisZohoOAuthToken {

  @Id
  @Column(name = "user_identifier")
  private String userIdentifier;
  @Column(name = "access_token")
  private String accessToken;
  @Column(name = "refresh_token")
  private String refreshToken;
  @Column(name = "expiry_time")
  private Long expiryTime;

  public MetisZohoOAuthToken() {
  }

  /**
   * Contains all the relative token information required for Zoho.
   *
   * @param userIdentifier the user identifier
   * @param accessToken the access token
   * @param refreshToken the refresh token
   * @param expiryTime the expiry time of the access token
   */
  public MetisZohoOAuthToken(String userIdentifier, String accessToken, String refreshToken,
      Long expiryTime) {
    this.userIdentifier = userIdentifier;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiryTime = expiryTime;
  }

  /**
   * Converts the current Metis object to the {@link ZohoOAuthTokens} object.
   *
   * @return the token represented as object of the Zoho library
   */
  public ZohoOAuthTokens convertToZohoOAuthTokens() {
    ZohoOAuthTokens zohoOAuthTokens = new ZohoOAuthTokens();
    zohoOAuthTokens.setUserMailId(this.userIdentifier);
    zohoOAuthTokens.setAccessToken(this.accessToken);
    zohoOAuthTokens.setRefreshToken(this.refreshToken);
    zohoOAuthTokens.setExpiryTime(this.expiryTime);

    return zohoOAuthTokens;
  }

  public String getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(String userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Long getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
  }
}
