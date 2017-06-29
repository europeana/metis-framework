package eu.europeana.metis.templates.page.landingpage.profile;

import java.util.HashMap;
import java.util.List;
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
    "label",
    "organizations"
})
public class SelectedOrganizations {

  @JsonProperty("label")
  private String label;
  @JsonProperty("organizations")
  private List<OrganizationModel> organizations = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  public SelectedOrganizations(String label,
      List<OrganizationModel> organizations) {
    this.label = label;
    this.organizations = organizations;
  }

  @JsonProperty("label")
  public String getLabel() {
    return label;
  }

  @JsonProperty("label")
  public void setLabel(String label) {
    this.label = label;
  }

  @JsonProperty("organizations")
  public List<OrganizationModel> getOrganizations() {
    return organizations;
  }

  @JsonProperty("organizations")
  public void setOrganizations(List<OrganizationModel> organizations) {
    this.organizations = organizations;
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