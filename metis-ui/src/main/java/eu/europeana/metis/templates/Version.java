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
    "is_alpha",
    "is_beta"
})
public class Version {

  @JsonProperty("is_alpha")
  private Boolean isAlpha;
  @JsonProperty("is_beta")
  private Boolean isBeta;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("is_alpha")
  public Boolean getIsAlpha() {
    return isAlpha;
  }

  @JsonProperty("is_alpha")
  public void setIsAlpha(Boolean isAlpha) {
    this.isAlpha = isAlpha;
  }

  @JsonProperty("is_beta")
  public Boolean getIsBeta() {
    return isBeta;
  }

  @JsonProperty("is_beta")
  public void setIsBeta(Boolean isBeta) {
    this.isBeta = isBeta;
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
