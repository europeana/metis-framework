package eu.europeana.metis.templates.page.landingpage.profile;

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
    "organizationName",
    "organizationId",
    "organizationRole",
    "rolesTypeId"
})
public class OrganizationModel {

  @JsonProperty("organizationName")
  private String organizationName;
  @JsonProperty("organizationId")
  private Integer organizationId;
  @JsonProperty("organizationRole")
  private String organizationRole;
  @JsonProperty("rolesTypeId")
  private Integer rolesTypeId;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  public OrganizationModel(String organizationName, Integer organizationId,
      String organizationRole, Integer rolesTypeId) {
    this.organizationName = organizationName;
    this.organizationId = organizationId;
    this.organizationRole = organizationRole;
    this.rolesTypeId = rolesTypeId;
  }

  @JsonProperty("organizationName")
  public String getOrganizationName() {
    return organizationName;
  }

  @JsonProperty("organizationName")
  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  @JsonProperty("organizationId")
  public Integer getOrganizationId() {
    return organizationId;
  }

  @JsonProperty("organizationId")
  public void setOrganizationId(Integer organizationId) {
    this.organizationId = organizationId;
  }

  @JsonProperty("organizationRole")
  public String getOrganizationRole() {
    return organizationRole;
  }

  @JsonProperty("organizationRole")
  public void setOrganizationRole(String organizationRole) {
    this.organizationRole = organizationRole;
  }

  @JsonProperty("rolesTypeId")
  public Integer getRolesTypeId() {
    return rolesTypeId;
  }

  @JsonProperty("rolesTypeId")
  public void setRolesTypeId(Integer rolesTypeId) {
    this.rolesTypeId = rolesTypeId;
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