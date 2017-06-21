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
 * @since 2017-06-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "update",
    "create",
    "preview",
    "value"
})
public class ViewMode {

  @JsonProperty("update")
  private Boolean update;
  @JsonProperty("create")
  private Boolean create;
  @JsonProperty("preview")
  private Boolean preview;
  @JsonProperty("value")
  private String value;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  public ViewMode(String mode) {
    switch (mode)
    {
      case "preview":
        preview = true;
        break;
      case "create":
        create = true;
        break;
      case "update":
        update = true;
        break;
    }
    value = mode;
  }

  @JsonProperty("update")
  public Boolean getUpdate() {
    return update;
  }

  @JsonProperty("update")
  public void setUpdate(Boolean update) {
    this.update = update;
  }

  @JsonProperty("create")
  public Boolean getCreate() {
    return create;
  }

  @JsonProperty("create")
  public void setCreate(Boolean create) {
    this.create = create;
  }

  @JsonProperty("preview")
  public Boolean getPreview() {
    return preview;
  }

  @JsonProperty("preview")
  public void setPreview(Boolean preview) {
    this.preview = preview;
  }

  @JsonProperty("value")
  public String getValue() {
    return value;
  }

  @JsonProperty("value")
  public void setValue(String value) {
    this.value = value;
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
