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
    "object_id",
    "tooltip",
    "occurence"
})
public class FieldValueCell {

  @JsonProperty("object_id")
  private String objectId;
  @JsonProperty("tooltip")
  private Tooltip tooltip;
  @JsonProperty("occurence")
  private String occurence;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("object_id")
  public String getObjectId() {
    return objectId;
  }

  @JsonProperty("object_id")
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  @JsonProperty("tooltip")
  public Tooltip getTooltip() {
    return tooltip;
  }

  @JsonProperty("tooltip")
  public void setTooltip(Tooltip tooltip) {
    this.tooltip = tooltip;
  }

  @JsonProperty("occurence")
  public String getOccurence() {
    return occurence;
  }

  @JsonProperty("occurence")
  public void setOccurence(String occurence) {
    this.occurence = occurence;
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
