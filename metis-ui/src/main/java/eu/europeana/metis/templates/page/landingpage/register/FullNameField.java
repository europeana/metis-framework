package eu.europeana.metis.templates.page.landingpage.register;

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
 * @since 2017-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label",
    "first_name_placeholder",
    "last_name_placeholder"
})
public class FullNameField {

  @JsonProperty("label")
  private String label;
  @JsonProperty("first_name_placeholder")
  private String firstNamePlaceholder;
  @JsonProperty("last_name_placeholder")
  private String lastNamePlaceholder;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("label")
  public String getLabel() {
    return label;
  }

  @JsonProperty("label")
  public void setLabel(String label) {
    this.label = label;
  }

  @JsonProperty("first_name_placeholder")
  public String getFirstNamePlaceholder() {
    return firstNamePlaceholder;
  }

  @JsonProperty("first_name_placeholder")
  public void setFirstNamePlaceholder(String firstNamePlaceholder) {
    this.firstNamePlaceholder = firstNamePlaceholder;
  }

  @JsonProperty("last_name_placeholder")
  public String getLastNamePlaceholder() {
    return lastNamePlaceholder;
  }

  @JsonProperty("last_name_placeholder")
  public void setLastNamePlaceholder(String lastNamePlaceholder) {
    this.lastNamePlaceholder = lastNamePlaceholder;
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
