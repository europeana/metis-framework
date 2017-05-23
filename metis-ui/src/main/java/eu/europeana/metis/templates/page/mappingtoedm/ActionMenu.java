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
    "sections",
    "search_box"
})
public class ActionMenu {

  @JsonProperty("sections")
  private List<Section> sections = null;
  @JsonProperty("search_box")
  private SearchBox searchBox;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("sections")
  public List<Section> getSections() {
    return sections;
  }

  @JsonProperty("sections")
  public void setSections(List<Section> sections) {
    this.sections = sections;
  }

  @JsonProperty("search_box")
  public SearchBox getSearchBox() {
    return searchBox;
  }

  @JsonProperty("search_box")
  public void setSearchBox(SearchBox searchBox) {
    this.searchBox = searchBox;
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
