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
    "start_value",
    "items"
})
public class Countries {

  @JsonProperty("label")
  private String label;
  @JsonProperty("start_value")
  private String startValue;
  @JsonProperty("items")
  private List<CountryItem> items = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("label")
  public String getLabel() {
    return label;
  }

  @JsonProperty("label")
  public void setLabel(String label) {
    this.label = label;
  }

  @JsonProperty("start_value")
  public String getStartValue() {
    return startValue;
  }

  @JsonProperty("start_value")
  public void setStartValue(String startValue) {
    this.startValue = startValue;
  }

  @JsonProperty("items")
  public List<CountryItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<CountryItem> items) {
    this.items = items;
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
