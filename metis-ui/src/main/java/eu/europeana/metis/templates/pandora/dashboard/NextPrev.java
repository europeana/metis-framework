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
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "next_url",
    "results_url",
    "prev_url"
})
public class NextPrev {

  @JsonProperty("next_url")
  private String nextUrl;
  @JsonProperty("results_url")
  private String resultsUrl;
  @JsonProperty("prev_url")
  private String prevUrl;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("next_url")
  public String getNextUrl() {
    return nextUrl;
  }

  @JsonProperty("next_url")
  public void setNextUrl(String nextUrl) {
    this.nextUrl = nextUrl;
  }

  @JsonProperty("results_url")
  public String getResultsUrl() {
    return resultsUrl;
  }

  @JsonProperty("results_url")
  public void setResultsUrl(String resultsUrl) {
    this.resultsUrl = resultsUrl;
  }

  @JsonProperty("prev_url")
  public String getPrevUrl() {
    return prevUrl;
  }

  @JsonProperty("prev_url")
  public void setPrevUrl(String prevUrl) {
    this.prevUrl = prevUrl;
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
