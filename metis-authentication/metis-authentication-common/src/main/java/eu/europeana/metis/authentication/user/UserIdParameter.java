package eu.europeana.metis.authentication.user;

/**
 * Contains the userId parameter.
 * <p>This class is used for passing parameters as json to a http request body. It contains the
 * userId parameter</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-02-07
 */
public class UserIdParameter {

  private String userId;

  public UserIdParameter() {
  }

  /**
   * Constructor with all required parameters
   *
   * @param userId the user identifier
   */
  public UserIdParameter(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
