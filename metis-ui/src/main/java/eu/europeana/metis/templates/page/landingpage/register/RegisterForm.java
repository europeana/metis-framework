package eu.europeana.metis.templates.page.landingpage.register;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "form_title",
    "full_name_field",
    "email_field",
    "password_field",
    "register_err_duplicate_user",
    "register_success_user_creation",
    "submit_btn_text",
    "reset_btn_text",
    "requirements"
})
public class RegisterForm {

  @JsonProperty("form_title")
  private String formTitle;
  @JsonProperty("full_name_field")
  private FullNameField fullNameField;
  @JsonProperty("email_field")
  private EmailField emailField;
  @JsonProperty("password_field")
  private PasswordField passwordField;
  @JsonProperty("register_err_duplicate_user")
  private String registerErrDuplicateUser;
  @JsonProperty("register_success_user_creation")
  private String registerSuccessUserCreation;
  @JsonProperty("submit_btn_text")
  private String submitBtnText;
  @JsonProperty("reset_btn_text")
  private String resetBtnText;
  @JsonProperty("form_requirements_warning")
  private String formRequirementsWarning;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("form_title")
  public String getFormTitle() {
    return formTitle;
  }

  @JsonProperty("form_title")
  public void setFormTitle(String formTitle) {
    this.formTitle = formTitle;
  }

  @JsonProperty("full_name_field")
  public FullNameField getFullNameField() {
    return fullNameField;
  }

  @JsonProperty("full_name_field")
  public void setFullNameField(FullNameField fullNameField) {
    this.fullNameField = fullNameField;
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

  @JsonProperty("register_err_duplicate_user")
  public String getRegisterErrDuplicateUser() {
    return registerErrDuplicateUser;
  }

  @JsonProperty("register_err_duplicate_user")
  public void setRegisterErrDuplicateUser(String registerErrDuplicateUser) {
    this.registerErrDuplicateUser = registerErrDuplicateUser;
  }

  @JsonProperty("register_success_user_creation")
  public String getRegisterSuccessUserCreation() {
    return registerSuccessUserCreation;
  }

  @JsonProperty("register_success_user_creation")
  public void setRegisterSuccessUserCreation(String registerSuccessUserCreation) {
    this.registerSuccessUserCreation = registerSuccessUserCreation;
  }

  @JsonProperty("submit_btn_text")
  public String getSubmitBtnText() {
    return submitBtnText;
  }

  @JsonProperty("submit_btn_text")
  public void setSubmitBtnText(String submitBtnText) {
    this.submitBtnText = submitBtnText;
  }

  @JsonProperty("reset_btn_text")
  public String getResetBtnText() {
    return resetBtnText;
  }

  @JsonProperty("reset_btn_text")
  public void setResetBtnText(String resetBtnText) {
    this.resetBtnText = resetBtnText;
  }

  @JsonProperty("form_requirements_warning")
  public String getFormRequirementsWarning() {
    return formRequirementsWarning;
  }

  @JsonProperty("form_requirements_warning")
  public void setFormRequirementsWarning(String formRequirementsWarning) {
    this.formRequirementsWarning = formRequirementsWarning;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
