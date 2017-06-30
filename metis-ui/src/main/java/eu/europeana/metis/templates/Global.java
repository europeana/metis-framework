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
 * @since 2017-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "primary_nav",
    "utility_nav",
    "options",
    "logo"
})
public class Global {

  @JsonProperty("primary_nav")
  private PrimaryNav primaryNav;
  @JsonProperty("utility_nav")
  private UtilityNav utilityNav;
  @JsonProperty("options")
  private Options options;
  @JsonProperty("logo")
  private Logo logo;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("primary_nav")
  public PrimaryNav getPrimaryNav() {
    return primaryNav;
  }

  @JsonProperty("primary_nav")
  public void setPrimaryNav(PrimaryNav primaryNav) {
    this.primaryNav = primaryNav;
  }

  @JsonProperty("utility_nav")
  public UtilityNav getUtilityNav() {
    return utilityNav;
  }

  @JsonProperty("utility_nav")
  public void setUtilityNav(UtilityNav utilityNav) {
    this.utilityNav = utilityNav;
  }
  @JsonProperty("options")
  public Options getOptions() {
    return options;
  }

  @JsonProperty("options")
  public void setOptions(Options options) {
    this.options = options;
  }

  @JsonProperty("logo")
  public Logo getLogo() {
    return logo;
  }

  @JsonProperty("logo")
  public void setLogo(Logo logo) {
    this.logo = logo;
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