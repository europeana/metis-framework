//package eu.europeana.metis.templates.page.landingpage;
//
//
//import java.util.HashMap;
//import java.util.Map;
//import com.fasterxml.jackson.annotation.JsonAnyGetter;
//import com.fasterxml.jackson.annotation.JsonAnySetter;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.annotation.JsonPropertyOrder;
//
///**
// * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
// * @since 2017-05-23
// */
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder({
//    "text",
//    "value",
//    "selected"
//})
//public class CountryModel {
//
//  @JsonProperty("text")
//  private String text;
//  @JsonProperty("value")
//  private String value;
//  @JsonProperty("selected")
//  private String selected;
//  @JsonIgnore
//  private Map<String, Object> additionalProperties = new HashMap<String, Object>();
//
//  @JsonProperty("text")
//  public String getText() {
//    return text;
//  }
//
//  @JsonProperty("text")
//  public void setText(String text) {
//    this.text = text;
//  }
//
//  @JsonProperty("value")
//  public String getValue() {
//    return value;
//  }
//
//  @JsonProperty("value")
//  public void setValue(String value) {
//    this.value = value;
//  }
//
//  @JsonProperty("selected")
//  public String getSelected() {
//    return selected;
//  }
//
//  @JsonProperty("selected")
//  public void setSelected(String selected) {
//    this.selected = selected;
//  }
//
//  @JsonAnyGetter
//  public Map<String, Object> getAdditionalProperties() {
//    return this.additionalProperties;
//  }
//
//  @JsonAnySetter
//  public void setAdditionalProperty(String name, Object value) {
//    this.additionalProperties.put(name, value);
//  }
//
//}
