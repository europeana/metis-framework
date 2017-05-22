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
    "search_active",
    "settings_active",
    "oursites_hidden"
})
public class Options {

  @JsonProperty("search_active")
  private Boolean searchActive;
  @JsonProperty("settings_active")
  private Boolean settingsActive;
  @JsonProperty("oursites_hidden")
  private Boolean oursitesHidden;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("search_active")
  public Boolean getSearchActive() {
    return searchActive;
  }

  @JsonProperty("search_active")
  public void setSearchActive(Boolean searchActive) {
    this.searchActive = searchActive;
  }

  @JsonProperty("settings_active")
  public Boolean getSettingsActive() {
    return settingsActive;
  }

  @JsonProperty("settings_active")
  public void setSettingsActive(Boolean settingsActive) {
    this.settingsActive = settingsActive;
  }

  @JsonProperty("oursites_hidden")
  public Boolean getOursitesHidden() {
    return oursitesHidden;
  }

  @JsonProperty("oursites_hidden")
  public void setOursitesHidden(Boolean oursitesHidden) {
    this.oursitesHidden = oursitesHidden;
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
