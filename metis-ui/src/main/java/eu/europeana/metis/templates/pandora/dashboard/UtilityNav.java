package eu.europeana.metis.templates.pandora.dashboard;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "style_modifier",
    "tabindex",
    "items",
    "menu_id"
})
public class UtilityNav {

  @JsonProperty("style_modifier")
  private String styleModifier;
  @JsonProperty("tabindex")
  private String tabindex;
  @JsonProperty("items")
  private List<MenuItem> items = null;
  @JsonProperty("menu_id")
  private String menuId;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("style_modifier")
  public String getStyleModifier() {
    return styleModifier;
  }

  @JsonProperty("style_modifier")
  public void setStyleModifier(String styleModifier) {
    this.styleModifier = styleModifier;
  }

  @JsonProperty("tabindex")
  public String getTabindex() {
    return tabindex;
  }

  @JsonProperty("tabindex")
  public void setTabindex(String tabindex) {
    this.tabindex = tabindex;
  }

  @JsonProperty("items")
  public List<MenuItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<MenuItem> items) {
    this.items = items;
  }

  @JsonProperty("menu_id")
  public String getMenuId() {
    return menuId;
  }

  @JsonProperty("menu_id")
  public void setMenuId(String menuId) {
    this.menuId = menuId;
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
