package eu.europeana.metis.templates.page.mappingtoedm;

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
    "tooltipped_text",
    "tooltip_text"
})
public class Tooltip {

  @JsonProperty("tooltipped_text")
  private String tooltippedText;
  @JsonProperty("tooltip_text")
  private String tooltipText;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("tooltipped_text")
  public String getTooltippedText() {
    return tooltippedText;
  }

  @JsonProperty("tooltipped_text")
  public void setTooltippedText(String tooltippedText) {
    this.tooltippedText = tooltippedText;
  }

  @JsonProperty("tooltip_text")
  public String getTooltipText() {
    return tooltipText;
  }

  @JsonProperty("tooltip_text")
  public void setTooltipText(String tooltipText) {
    this.tooltipText = tooltipText;
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
