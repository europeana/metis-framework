package eu.europeana.metis.templates.pandora.dashboard;

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
    "subfooter",
    "social",
    "linklist1",
    "linklist3",
    "linklist2"
})
public class Footer {

  @JsonProperty("subfooter")
  private SubFooter subfooter;
  @JsonProperty("social")
  private Social social;
  @JsonProperty("linklist1")
  private ListOfLinks linklist1;
  @JsonProperty("linklist3")
  private ListOfLinks linklist3;
  @JsonProperty("linklist2")
  private ListOfLinks linklist2;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("subfooter")
  public SubFooter getSubfooter() {
    return subfooter;
  }

  @JsonProperty("subfooter")
  public void setSubfooter(SubFooter subfooter) {
    this.subfooter = subfooter;
  }

  @JsonProperty("social")
  public Social getSocial() {
    return social;
  }

  @JsonProperty("social")
  public void setSocial(Social social) {
    this.social = social;
  }

  @JsonProperty("linklist1")
  public ListOfLinks getLinklist1() {
    return linklist1;
  }

  @JsonProperty("linklist1")
  public void setLinklist1(ListOfLinks linklist1) {
    this.linklist1 = linklist1;
  }

  @JsonProperty("linklist3")
  public ListOfLinks getLinklist3() {
    return linklist3;
  }

  @JsonProperty("linklist3")
  public void setLinklist3(ListOfLinks linklist3) {
    this.linklist3 = linklist3;
  }

  @JsonProperty("linklist2")
  public ListOfLinks getLinklist2() {
    return linklist2;
  }

  @JsonProperty("linklist2")
  public void setLinklist2(ListOfLinks linklist2) {
    this.linklist2 = linklist2;
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
