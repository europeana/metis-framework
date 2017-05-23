package eu.europeana.metis.templates.pandora.dashboard;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-22
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "text",
    "url",
    "icon",
    "is_current",
    "is_divider",
    "subtitle",
    "message",
    "submenu"
})
public class SubmenuItem {
  @JsonProperty("text")
  private String text;
  @JsonProperty("url")
  private String url;
  @JsonProperty("icon")
  private String icon;
  @JsonProperty("is_current")
  private Boolean isCurrent;
  @JsonProperty("is_divider")
  private Object isDivider;
  @JsonProperty("subtitle")
  private Object subtitle;
  @JsonProperty("message")
  private Object message;
  @JsonProperty("submenu")
  private Boolean submenu;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("text")
  public String getText() {
    return text;
  }

  @JsonProperty("text")
  public void setText(String text) {
    this.text = text;
  }

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }

  @JsonProperty("icon")
  public String getIcon() {
    return icon;
  }

  @JsonProperty("icon")
  public void setIcon(String icon) {
    this.icon = icon;
  }

  @JsonProperty("is_current")
  public Boolean getIsCurrent() {
    return isCurrent;
  }

  @JsonProperty("is_current")
  public void setIsCurrent(Boolean isCurrent) {
    this.isCurrent = isCurrent;
  }

  @JsonProperty("is_divider")
  public Object getIsDivider() {
    return isDivider;
  }

  @JsonProperty("is_divider")
  public void setIsDivider(Object isDivider) {
    this.isDivider = isDivider;
  }

  @JsonProperty("subtitle")
  public Object getSubtitle() {
    return subtitle;
  }

  @JsonProperty("subtitle")
  public void setSubtitle(Object subtitle) {
    this.subtitle = subtitle;
  }

  @JsonProperty("message")
  public Object getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(Object message) {
    this.message = message;
  }

  @JsonProperty("submenu")
  public Boolean getSubmenu() {
    return submenu;
  }

  @JsonProperty("submenu")
  public void setSubmenu(Boolean submenu) {
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
