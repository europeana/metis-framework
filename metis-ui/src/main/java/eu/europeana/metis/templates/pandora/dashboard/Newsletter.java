package eu.europeana.metis.templates.pandora.dashboard;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "submit-alt",
    "email-address-invalid",
    "choose-language",
    "email-address-required",
    "signup",
    "language-required"
})
public class Newsletter {

  @JsonProperty("submit-alt")
  private String submitAlt;
  @JsonProperty("email-address-invalid")
  private String emailAddressInvalid;
  @JsonProperty("choose-language")
  private String chooseLanguage;
  @JsonProperty("email-address-required")
  private String emailAddressRequired;
  @JsonProperty("signup")
  private String signup;
  @JsonProperty("language-required")
  private String languageRequired;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("submit-alt")
  public String getSubmitAlt() {
    return submitAlt;
  }

  @JsonProperty("submit-alt")
  public void setSubmitAlt(String submitAlt) {
    this.submitAlt = submitAlt;
  }

  @JsonProperty("email-address-invalid")
  public String getEmailAddressInvalid() {
    return emailAddressInvalid;
  }

  @JsonProperty("email-address-invalid")
  public void setEmailAddressInvalid(String emailAddressInvalid) {
    this.emailAddressInvalid = emailAddressInvalid;
  }

  @JsonProperty("choose-language")
  public String getChooseLanguage() {
    return chooseLanguage;
  }

  @JsonProperty("choose-language")
  public void setChooseLanguage(String chooseLanguage) {
    this.chooseLanguage = chooseLanguage;
  }

  @JsonProperty("email-address-required")
  public String getEmailAddressRequired() {
    return emailAddressRequired;
  }

  @JsonProperty("email-address-required")
  public void setEmailAddressRequired(String emailAddressRequired) {
    this.emailAddressRequired = emailAddressRequired;
  }

  @JsonProperty("signup")
  public String getSignup() {
    return signup;
  }

  @JsonProperty("signup")
  public void setSignup(String signup) {
    this.signup = signup;
  }

  @JsonProperty("language-required")
  public String getLanguageRequired() {
    return languageRequired;
  }

  @JsonProperty("language-required")
  public void setLanguageRequired(String languageRequired) {
    this.languageRequired = languageRequired;
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
