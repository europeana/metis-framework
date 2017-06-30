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
    "search_box_legend",
    "search_box_label",
    "search_box_hidden"
})
public class SearchBox {

  @JsonProperty("search_box_legend")
  private String searchBoxLegend;
  @JsonProperty("search_box_label")
  private String searchBoxLabel;
  @JsonProperty("search_box_hidden")
  private String searchBoxHidden;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("search_box_legend")
  public String getSearchBoxLegend() {
    return searchBoxLegend;
  }

  @JsonProperty("search_box_legend")
  public void setSearchBoxLegend(String searchBoxLegend) {
    this.searchBoxLegend = searchBoxLegend;
  }

  @JsonProperty("search_box_label")
  public String getSearchBoxLabel() {
    return searchBoxLabel;
  }

  @JsonProperty("search_box_label")
  public void setSearchBoxLabel(String searchBoxLabel) {
    this.searchBoxLabel = searchBoxLabel;
  }

  @JsonProperty("search_box_hidden")
  public String getSearchBoxHidden() {
    return searchBoxHidden;
  }

  @JsonProperty("search_box_hidden")
  public void setSearchBoxHidden(String searchBoxHidden) {
    this.searchBoxHidden = searchBoxHidden;
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
