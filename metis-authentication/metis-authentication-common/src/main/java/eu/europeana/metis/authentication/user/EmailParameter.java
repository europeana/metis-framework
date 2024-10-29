package eu.europeana.metis.authentication.user;

/**
 * Contains the email parameter.
 * <p>This class is used for passing parameters as json to a http request body. It contains the
 * email parameter</p>
 */
public class EmailParameter {

  private String email;

  public EmailParameter() {
  }

  /**
   * Constructor with all required parameters
   *
   * @param email the email parameter
   */
  public EmailParameter(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
