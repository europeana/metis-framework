package eu.europeana.metis.authentication.user;

/**
 * Contains the email and password of a user temporarily.
 * <p>Used mostly after decoding the Authorization Header in an HTTP request.</p>
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-14
 */
public class Credentials {

  private String email;
  private String password;

  /**
   * Contstructor with required fields.
   * @param email the email of the user
   * @param password the passsword of the user
   */
  public Credentials(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
