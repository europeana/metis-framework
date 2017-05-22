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
    "title",
    "input_name",
    "placeholder"
})
public class InputSearch {

  @JsonProperty("title")
  private String title;
  @JsonProperty("input_name")
  private String inputName;
  @JsonProperty("placeholder")
  private String placeholder;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("input_name")
  public String getInputName() {
    return inputName;
  }

  @JsonProperty("input_name")
  public void setInputName(String inputName) {
    this.inputName = inputName;
  }

  @JsonProperty("placeholder")
  public String getPlaceholder() {
    return placeholder;
  }

  @JsonProperty("placeholder")
  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
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
