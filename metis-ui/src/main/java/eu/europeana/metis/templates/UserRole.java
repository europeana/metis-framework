package eu.europeana.metis.templates;

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
 * @since 2017-06-20
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "metisAdmin",
    "metisUser",
    "value"
})
public class UserRole {

  @JsonProperty("metisAdmin")
  private Boolean metisAdmin;
  @JsonProperty("metisUser")
  private Boolean metisUser;
  @JsonProperty("value")
  private String value;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  public UserRole(String userRole) {
    switch (userRole)
    {
      case "metisAdmin":
        metisAdmin = true;
        break;
      case "metisUser":
        metisUser = true;
        break;
    }
    value = userRole;
  }

  @JsonProperty("metisAdmin")
  public Boolean getMetisAdmin() {
    return metisAdmin;
  }

  @JsonProperty("metisAdmin")
  public void setMetisAdmin(Boolean metisAdmin) {
    this.metisAdmin = metisAdmin;
  }

  @JsonProperty("metisUser")
  public Boolean getMetisUser() {
    return metisUser;
  }

  @JsonProperty("metisUser")
  public void setMetisUser(Boolean metisUser) {
    this.metisUser = metisUser;
  }

  @JsonProperty("value")
  public String getValue() {
    return value;
  }

  @JsonProperty("value")
  public void setValue(String value) {
    this.value = value;
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