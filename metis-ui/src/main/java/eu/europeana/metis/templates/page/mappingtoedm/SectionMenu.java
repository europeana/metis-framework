package eu.europeana.metis.templates.page.mappingtoedm;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.SubmenuItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "button_title",
    "menu_title",
    "menu_id",
    "style_modifier",
    "items"
})
public class SectionMenu {

  @JsonProperty("button_title")
  private String buttonTitle;
  @JsonProperty("menu_title")
  private String menuTitle;
  @JsonProperty("menu_id")
  private String menuId;
  @JsonProperty("style_modifier")
  private String styleModifier;
  @JsonProperty("items")
  private List<SubmenuItem> items = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("button_title")
  public String getButtonTitle() {
    return buttonTitle;
  }

  @JsonProperty("button_title")
  public void setButtonTitle(String buttonTitle) {
    this.buttonTitle = buttonTitle;
  }

  @JsonProperty("menu_title")
  public String getMenuTitle() {
    return menuTitle;
  }

  @JsonProperty("menu_title")
  public void setMenuTitle(String menuTitle) {
    this.menuTitle = menuTitle;
  }

  @JsonProperty("menu_id")
  public String getMenuId() {
    return menuId;
  }

  @JsonProperty("menu_id")
  public void setMenuId(String menuId) {
    this.menuId = menuId;
  }

  @JsonProperty("style_modifier")
  public String getStyleModifier() {
    return styleModifier;
  }

  @JsonProperty("style_modifier")
  public void setStyleModifier(String styleModifier) {
    this.styleModifier = styleModifier;
  }

  @JsonProperty("items")
  public List<SubmenuItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<SubmenuItem> items) {
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
