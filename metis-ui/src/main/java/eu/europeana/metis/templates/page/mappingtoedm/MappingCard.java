package eu.europeana.metis.templates.page.mappingtoedm;

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
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "object_id",
    "prefix",
    "name",
    "dropdown",
    "field_value_cells"
})
public class MappingCard {

  @JsonProperty("object_id")
  private String objectId;
  @JsonProperty("prefix")
  private String prefix;
  @JsonProperty("name")
  private String name;
  @JsonProperty("dropdown")
  private Dropdown dropdown;
  @JsonProperty("field_value_cells")
  private List<FieldValueCell> fieldValueCells = null;
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

  @JsonProperty("prefix")
  public String getPrefix() {
    return prefix;
  }

  @JsonProperty("prefix")
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("dropdown")
  public Dropdown getDropdown() {
    return dropdown;
  }

  @JsonProperty("dropdown")
  public void setDropdown(Dropdown dropdown) {
    this.dropdown = dropdown;
  }

  @JsonProperty("field_value_cells")
  public List<FieldValueCell> getFieldValueCells() {
    return fieldValueCells;
  }

  @JsonProperty("field_value_cells")
  public void setFieldValueCells(List<FieldValueCell> fieldValueCells) {
    this.fieldValueCells = fieldValueCells;
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
