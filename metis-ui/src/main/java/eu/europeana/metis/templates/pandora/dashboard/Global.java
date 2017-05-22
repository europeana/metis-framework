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
 * @since 2017-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "options",
    "logo"
})
public class Global {

  @JsonProperty("options")
  private Options options;
  @JsonProperty("logo")
  private Logo logo;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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