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
    "authentication_error_message"
})
public class LoginErrAuthenticate {

  @JsonProperty("authentication_error_message")
  private String authenticationErrorMessage;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("authentication_error_message")
  public String getAuthenticationErrorMessage() {
    return authenticationErrorMessage;
  }

  @JsonProperty("authentication_error_message")
  public void setAuthenticationErrorMessage(String authenticationErrorMessage) {
    this.authenticationErrorMessage = authenticationErrorMessage;
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
