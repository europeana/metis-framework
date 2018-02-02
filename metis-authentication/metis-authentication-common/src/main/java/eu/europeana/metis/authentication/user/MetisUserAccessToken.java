package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-30
 */
@Entity
@Table(name = "metis_user_access_tokens")
public class MetisUserAccessToken {
  @Id
  @Column(name = "email")
  @JsonIgnore
  private String email;
  @Column(name = "access_token")
  private String accessToken;
  @Column(name = "timestamp")
  @Temporal(TemporalType.TIMESTAMP)
  @JsonIgnore
  private Date timestamp;

  public MetisUserAccessToken() {
    //Required for json serialization
  }

  /**
   * Contains the access token information of a user.
   * @param email the email of the user
   * @param accessToken the String representation of an access token
   * @param timestamp the timestamp of the token
   */
  public MetisUserAccessToken(String email, String accessToken, Date timestamp) {
    this.email = email;
    this.accessToken = accessToken;
    this.timestamp = timestamp == null ? null : new Date(timestamp.getTime());
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public Date getTimestamp() {
    return timestamp == null ? null : new Date(timestamp.getTime());
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp == null ? null : new Date(timestamp.getTime());
  }
}
