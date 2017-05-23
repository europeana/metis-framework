package eu.europeana.metis.templates.page.landingpage;

import eu.europeana.metis.templates.Content;
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
    "is_home",
    "is_login",
    "is_register",
    "is_profile",
    "is_requests",
    "is_request_approve",
    "content"
})
public class LandingPageContent {

  @JsonProperty("is_home")
  private Boolean isHome;
  @JsonProperty("is_login")
  private Boolean isLogin;
  @JsonProperty("is_register")
  private Boolean isRegister;
  @JsonProperty("is_profile")
  private Boolean isProfile;
  @JsonProperty("is_requests")
  private Boolean isRequests;
  @JsonProperty("is_request_approve")
  private Boolean isRequestApprove;
  @JsonProperty("content")
  private Content content;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("is_home")
  public Boolean getIsHome() {
    return isHome;
  }

  @JsonProperty("is_home")
  public void setIsHome(Boolean isHome) {
    this.isHome = isHome;
  }

  @JsonProperty("is_login")
  public Boolean getIsLogin() {
    return isLogin;
  }

  @JsonProperty("is_login")
  public void setIsLogin(Boolean isLogin) {
    this.isLogin = isLogin;
  }

  @JsonProperty("is_register")
  public Boolean getIsRegister() {
    return isRegister;
  }

  @JsonProperty("is_register")
  public void setIsRegister(Boolean isRegister) {
    this.isRegister = isRegister;
  }

  @JsonProperty("is_profile")
  public Boolean getIsProfile() {
    return isProfile;
  }

  @JsonProperty("is_profile")
  public void setIsProfile(Boolean isProfile) {
    this.isProfile = isProfile;
  }

  @JsonProperty("is_requests")
  public Boolean getIsRequests() {
    return isRequests;
  }

  @JsonProperty("is_requests")
  public void setIsRequests(Boolean isRequests) {
    this.isRequests = isRequests;
  }

  @JsonProperty("is_request_approve")
  public Boolean getIsRequestApprove() {
    return isRequestApprove;
  }

  @JsonProperty("is_request_approve")
  public void setIsRequestApprove(Boolean isRequestApprove) {
    this.isRequestApprove = isRequestApprove;
  }

  @JsonProperty("content")
  public Content getContent() {
    return content;
  }

  @JsonProperty("content")
  public void setContent(Content content) {
    this.content = content;
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
