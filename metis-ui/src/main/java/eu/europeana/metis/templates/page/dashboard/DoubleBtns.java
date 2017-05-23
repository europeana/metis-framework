package eu.europeana.metis.templates.page.dashboard;

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
    "btn_left_text",
    "btn-right-text",
    "btn-right-under-text",
    "url-left",
    "url-right"
})
public class DoubleBtns {

  @JsonProperty("btn_left_text")
  private String btnLeftText;
  @JsonProperty("btn_right_text")
  private String btnRightText;
  @JsonProperty("btn_right_under_text")
  private String btnRightUnderText;
  @JsonProperty("url_left")
  private String urlLeft;
  @JsonProperty("url_right")
  private String urlRight;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("btn_left_text")
  public String getBtnLeftText() {
    return btnLeftText;
  }

  @JsonProperty("btn_left_text")
  public void setBtnLeftText(String btnLeftText) {
    this.btnLeftText = btnLeftText;
  }

  @JsonProperty("btn_right_text")
  public String getBtnRightText() {
    return btnRightText;
  }

  @JsonProperty("btn_right_text")
  public void setBtnRightText(String btnRightText) {
    this.btnRightText = btnRightText;
  }

  @JsonProperty("btn_right_under_text")
  public String getBtnRightUnderText() {
    return btnRightUnderText;
  }

  @JsonProperty("btn_right_under_text")
  public void setBtnRightUnderText(String btnRightUnderText) {
    this.btnRightUnderText = btnRightUnderText;
  }

  @JsonProperty("url_left")
  public String getUrlLeft() {
    return urlLeft;
  }

  @JsonProperty("url_left")
  public void setUrlLeft(String urlLeft) {
    this.urlLeft = urlLeft;
  }

  @JsonProperty("url_right")
  public String getUrlRight() {
    return urlRight;
  }

  @JsonProperty("url_right")
  public void setUrlRight(String urlRight) {
    this.urlRight = urlRight;
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
