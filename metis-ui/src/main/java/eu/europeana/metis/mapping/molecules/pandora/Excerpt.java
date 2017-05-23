package eu.europeana.metis.mapping.molecules.pandora;

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
    "vshort",
    "short",
    "medium",
    "long"
})
public class Excerpt {

  @JsonProperty("vshort")
  private String vshort;
  @JsonProperty("short")
  private String _short;
  @JsonProperty("medium")
  private String medium;
  @JsonProperty("long")
  private String _long;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("vshort")
  public String getVshort() {
    return vshort;
  }

  @JsonProperty("vshort")
  public void setVshort(String vshort) {
    this.vshort = vshort;
  }

  @JsonProperty("short")
  public String getShort() {
    return _short;
  }

  @JsonProperty("short")
  public void setShort(String _short) {
    this._short = _short;
  }

  @JsonProperty("medium")
  public String getMedium() {
    return medium;
  }

  @JsonProperty("medium")
  public void setMedium(String medium) {
    this.medium = medium;
  }

  @JsonProperty("long")
  public String getLong() {
    return _long;
  }

  @JsonProperty("long")
  public void setLong(String _long) {
    this._long = _long;
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
