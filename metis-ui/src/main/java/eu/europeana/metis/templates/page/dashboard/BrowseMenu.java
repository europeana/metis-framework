package eu.europeana.metis.templates.page.dashboard;

import eu.europeana.metis.templates.MenuItem;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
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
    "menu_id",
    "items"
})
public class BrowseMenu {

  @JsonProperty("menu_id")
  private String menuId;
  @JsonProperty("items")
  private List<MenuItem> items = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("menu_id")
  public String getMenuId() {
    return menuId;
  }

  @JsonProperty("menu_id")
  public void setMenuId(String menuId) {
    this.menuId = menuId;
  }

  @JsonProperty("items")
  public List<MenuItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<MenuItem> items) {
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
