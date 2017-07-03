package eu.europeana.metis.templates.page.landingpage;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.page.landingpage.register.EmailField;
import eu.europeana.metis.templates.page.landingpage.register.PasswordField;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "form_title",
    "email_field",
    "password_field",
    "forgot_login_credentials",
    "login_err_authenticate",
    "submit_btn"
})
public class LoginForm {

  @JsonProperty("login_url")
  private String loginUrl;
  @JsonProperty("form_title")
  private String formTitle;
  @JsonProperty("email_field")
  private EmailField emailField;
  @JsonProperty("password_field")
  private PasswordField passwordField;
  @JsonProperty("forgot_login_credentials")
  private ForgotLoginCredentials forgotLoginCredentials;
  @JsonProperty("login_err_authenticate")
  private LoginErrAuthenticate loginErrAuthenticate;
  @JsonProperty("submit_btn")
  private String submitBtn;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("form_title")
  public String getFormTitle() {
    return formTitle;
  }

  @JsonProperty("form_title")
  public void setFormTitle(String formTitle) {
    this.formTitle = formTitle;
  }

  @JsonProperty("email_field")
  public EmailField getEmailField() {
    return emailField;
  }

  @JsonProperty("email_field")
  public void setEmailField(EmailField emailField) {
    this.emailField = emailField;
  }

  @JsonProperty("password_field")
  public PasswordField getPasswordField() {
    return passwordField;
  }

  @JsonProperty("password_field")
  public void setPasswordField(PasswordField passwordField) {
    this.passwordField = passwordField;
  }

  @JsonProperty("forgot_login_credentials")
  public ForgotLoginCredentials getForgotLoginCredentials() {
    return forgotLoginCredentials;
  }

  @JsonProperty("forgot_login_credentials")
  public void setForgotLoginCredentials(ForgotLoginCredentials forgotLoginCredentials) {
    this.forgotLoginCredentials = forgotLoginCredentials;
  }

  @JsonProperty("login_err_authenticate")
  public LoginErrAuthenticate getLoginErrAuthenticate() {
    return loginErrAuthenticate;
  }

  @JsonProperty("login_err_authenticate")
  public void setLoginErrAuthenticate(
      LoginErrAuthenticate loginErrAuthenticate) {
    this.loginErrAuthenticate = loginErrAuthenticate;
  }

  @JsonProperty("submit_btn")
  public String getSubmitBtn() {
    return submitBtn;
  }

  @JsonProperty("submit_btn")
  public void setSubmitBtn(String submitBtn) {
    this.submitBtn = submitBtn;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
  @JsonProperty("login_url")
  public String getLoginUrl() {return loginUrl;}

  @JsonProperty("login_url")
  public void setLoginUrl(String loginUrl) {this.loginUrl = loginUrl;}
}
