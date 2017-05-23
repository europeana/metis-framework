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
    "github",
    "twitter",
    "facebook",
    "pinterest",
    "linkedin",
    "googleplus"
})
public class Social {

  @JsonProperty("github")
  private Boolean github;
  @JsonProperty("twitter")
  private Boolean twitter;
  @JsonProperty("facebook")
  private Boolean facebook;
  @JsonProperty("pinterest")
  private Boolean pinterest;
  @JsonProperty("linkedin")
  private Boolean linkedin;
  @JsonProperty("googleplus")
  private Boolean googleplus;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("github")
  public Boolean getGithub() {
    return github;
  }

  @JsonProperty("github")
  public void setGithub(Boolean github) {
    this.github = github;
  }

  @JsonProperty("twitter")
  public Boolean getTwitter() {
    return twitter;
  }

  @JsonProperty("twitter")
  public void setTwitter(Boolean twitter) {
    this.twitter = twitter;
  }

  @JsonProperty("facebook")
  public Boolean getFacebook() {
    return facebook;
  }

  @JsonProperty("facebook")
  public void setFacebook(Boolean facebook) {
    this.facebook = facebook;
  }

  @JsonProperty("pinterest")
  public Boolean getPinterest() {
    return pinterest;
  }

  @JsonProperty("pinterest")
  public void setPinterest(Boolean pinterest) {
    this.pinterest = pinterest;
  }

  @JsonProperty("linkedin")
  public Boolean getLinkedin() {
    return linkedin;
  }

  @JsonProperty("linkedin")
  public void setLinkedin(Boolean linkedin) {
    this.linkedin = linkedin;
  }

  @JsonProperty("googleplus")
  public Boolean getGoogleplus() {
    return googleplus;
  }

  @JsonProperty("googleplus")
  public void setGoogleplus(Boolean googleplus) {
    this.googleplus = googleplus;
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
