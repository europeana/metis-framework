package eu.europeana.metis.templates.page.landingpage;

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
    "cta_text",
    "text",
    "title",
    "info_link",
    "info_url",
    "cta_url"
})
public class Banner {

  @JsonProperty("cta_text")
  private String ctaText;
  @JsonProperty("text")
  private String text;
  @JsonProperty("title")
  private String title;
  @JsonProperty("info_link")
  private String infoLink;
  @JsonProperty("info_url")
  private String infoUrl;
  @JsonProperty("cta_url")
  private String ctaUrl;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("cta_text")
  public String getCtaText() {
    return ctaText;
  }

  @JsonProperty("cta_text")
  public void setCtaText(String ctaText) {
    this.ctaText = ctaText;
  }

  @JsonProperty("text")
  public String getText() {
    return text;
  }

  @JsonProperty("text")
  public void setText(String text) {
    this.text = text;
  }

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("info_link")
  public String getInfoLink() {
    return infoLink;
  }

  @JsonProperty("info_link")
  public void setInfoLink(String infoLink) {
    this.infoLink = infoLink;
  }

  @JsonProperty("info_url")
  public String getInfoUrl() {
    return infoUrl;
  }

  @JsonProperty("info_url")
  public void setInfoUrl(String infoUrl) {
    this.infoUrl = infoUrl;
  }

  @JsonProperty("cta_url")
  public String getCtaUrl() {
    return ctaUrl;
  }

  @JsonProperty("cta_url")
  public void setCtaUrl(String ctaUrl) {
    this.ctaUrl = ctaUrl;
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
