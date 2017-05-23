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
    "home_url",
    "home_text",
    "next_prev",
    "footer",
    "global"
})
public class Navigation {

  @JsonProperty("home_url")
  private String homeUrl;
  @JsonProperty("home_text")
  private String homeText;
  @JsonProperty("next_prev")
  private NextPrev nextPrev;
  @JsonProperty("footer")
  private Boolean footer;
  @JsonProperty("global")
  private Global global;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("home_url")
  public String getHomeUrl() {
    return homeUrl;
  }

  @JsonProperty("home_url")
  public void setHomeUrl(String homeUrl) {
    this.homeUrl = homeUrl;
  }

  @JsonProperty("home_text")
  public String getHomeText() {
    return homeText;
  }

  @JsonProperty("home_text")
  public void setHomeText(String homeText) {
    this.homeText = homeText;
  }

  @JsonProperty("next_prev")
  public NextPrev getNextPrev() {
    return nextPrev;
  }

  @JsonProperty("next_prev")
  public void setNextPrev(NextPrev nextPrev) {
    this.nextPrev = nextPrev;
  }

  @JsonProperty("footer")
  public Boolean getFooter() {
    return footer;
  }

  @JsonProperty("footer")
  public void setFooter(Boolean footer) {
    this.footer = footer;
  }

  @JsonProperty("global")
  public Global getGlobal() {
    return global;
  }

  @JsonProperty("global")
  public void setGlobal(Global global) {
    this.global = global;
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
