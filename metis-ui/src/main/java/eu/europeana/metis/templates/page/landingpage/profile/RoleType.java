package eu.europeana.metis.templates.page.landingpage.profile;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-20
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "typeId",
    "roles"
})
public class RoleType {

  @JsonProperty("typeId")
  private String typeId;
  @JsonProperty("roles")
  private List<RoleModel> roles = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  public RoleType(String typeId,
      List<RoleModel> roles) {
    this.typeId = typeId;
    this.roles = roles;
  }

  @JsonProperty("typeId")
  public String getTypeId() {
    return typeId;
  }

  @JsonProperty("typeId")
  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  @JsonProperty("roles")
  public List<RoleModel> getRoles() {
    return roles;
  }

  @JsonProperty("roles")
  public void setRoles(List<RoleModel> roles) {
    this.roles = roles;
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
