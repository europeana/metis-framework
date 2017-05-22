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
    "text_first",
    "user_name",
    "text_end"
})
public class WelcomeMessage {

  @JsonProperty("text_first")
  private String textFirst;
  @JsonProperty("user_name")
  private String userName;
  @JsonProperty("text_end")
  private String textEnd;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("text_first")
  public String getTextFirst() {
    return textFirst;
  }

  @JsonProperty("text_first")
  public void setTextFirst(String textFirst) {
    this.textFirst = textFirst;
  }

  @JsonProperty("user_name")
  public String getUserName() {
    return userName;
  }

  @JsonProperty("user_name")
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @JsonProperty("text_end")
  public String getTextEnd() {
    return textEnd;
  }

  @JsonProperty("text_end")
  public void setTextEnd(String textEnd) {
    this.textEnd = textEnd;
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
