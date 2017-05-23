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
 * @since 2017-05-22
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "url",
    "text",
    "text_mobile",
    "icon",
    "icon_class",
    "fontawesome",
    "submenu"
})
public class MenuItem {

  @JsonProperty("url")
  private String url;
  @JsonProperty("text")
  private String text;
  @JsonProperty("text_mobile")
  private String textMobile;
  @JsonProperty("icon")
  private String icon;
  @JsonProperty("icon_class")
  private String iconClass;
  @JsonProperty("fontawesome")
  private Boolean fontawesome;
  @JsonProperty("submenu")
  private Submenu submenu;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }

  @JsonProperty("text")
  public String getText() {
    return text;
  }

  @JsonProperty("text")
  public void setText(String text) {
    this.text = text;
  }

  @JsonProperty("text_mobile")
  public String getTextMobile() {
    return textMobile;
  }

  @JsonProperty("text_mobile")
  public void setTextMobile(String textMobile) {
    this.textMobile = textMobile;
  }

  @JsonProperty("icon")
  public String getIcon() {
    return icon;
  }

  @JsonProperty("icon")
  public void setIcon(String icon) {
    this.icon = icon;
  }

  @JsonProperty("icon_class")
  public String getIconClass() {
    return iconClass;
  }

  @JsonProperty("icon_class")
  public void setIconClass(String iconClass) {
    this.iconClass = iconClass;
  }

  @JsonProperty("fontawesome")
  public Boolean getFontawesome() {
    return fontawesome;
  }

  @JsonProperty("fontawesome")
  public void setFontawesome(Boolean fontawesome) {
    this.fontawesome = fontawesome;
  }

  @JsonProperty("submenu")
  public Submenu getSubmenu() {
    return submenu;
  }

  @JsonProperty("submenu")
  public void setSubmenu(Submenu submenu) {
    this.submenu = submenu;
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
