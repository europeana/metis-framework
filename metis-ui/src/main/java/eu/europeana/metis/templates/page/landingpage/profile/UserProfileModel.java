package eu.europeana.metis.templates.page.landingpage.profile;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.UserProfileViewMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "roleTypes",
    "user_fields"
})
public class UserProfileModel {
  @JsonProperty("userProfileUrl")
  private String userProfileUrl;

  @JsonProperty("roleTypes")
  private List<RoleType> roleTypes = null;
  @JsonProperty("user_fields")
  private UserFields userFields;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();



  @JsonProperty("view_mode")
  private UserProfileViewMode viewMode;

  @JsonProperty("roleTypes")
  public List<RoleType> getRoleTypes() {
    return roleTypes;
  }

  @JsonProperty("roleTypes")
  public void setRoleTypes(List<RoleType> roleTypes) {
    this.roleTypes = roleTypes;
  }

  @JsonProperty("user_fields")
  public UserFields getUserFields() {
    return userFields;
  }

  @JsonProperty("user_fields")
  public void setUserFields(UserFields userFields) {
    this.userFields = userFields;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
  @JsonProperty("userProfileUrl")
  public String getUserProfileUrl() { return userProfileUrl; }

  @JsonProperty("userProfileUrl")
  public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = userProfileUrl; }

  @JsonProperty("view_mode")
  public void setViewMode(UserProfileViewMode viewMode) {
    this.viewMode = viewMode;
  }
  @JsonProperty("view_mode")
  public UserProfileViewMode getViewMode() {
    return viewMode;
  }
}
