package eu.europeana.metis.authentication.user;

/**
 * Contains the old and new password parameters.
 * <p>This class is used for passing parameters as json to a http request body. It contains the old
 * password to be changed with the new provided password</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-02-07
 */
public class OldNewPasswordParameters {

  private String oldPassword;
  private String newPassword;

  public OldNewPasswordParameters() {
  }

  /**
   * Constructor with all required parameters
   *
   * @param oldPassword the old password
   * @param newPassword the new password
   */
  public OldNewPasswordParameters(String oldPassword, String newPassword) {
    this.oldPassword = oldPassword;
    this.newPassword = newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
