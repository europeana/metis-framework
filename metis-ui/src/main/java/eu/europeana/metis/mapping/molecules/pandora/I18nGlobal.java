package eu.europeana.metis.mapping.molecules.pandora;

import eu.europeana.metis.templates.pandora.dashboard.Newsletter;
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
    "newsletter",
    "mission",
    "mission-title",
    "find-us-elsewhere"
})
public class I18nGlobal {

  @JsonProperty("newsletter")
  private Newsletter newsletter;
  @JsonProperty("mission")
  private String mission;
  @JsonProperty("mission-title")
  private String missionTitle;
  @JsonProperty("find-us-elsewhere")
  private String findUsElsewhere;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("newsletter")
  public Newsletter getNewsletter() {
    return newsletter;
  }

  @JsonProperty("newsletter")
  public void setNewsletter(Newsletter newsletter) {
    this.newsletter = newsletter;
  }

  @JsonProperty("mission")
  public String getMission() {
    return mission;
  }

  @JsonProperty("mission")
  public void setMission(String mission) {
    this.mission = mission;
  }

  @JsonProperty("mission-title")
  public String getMissionTitle() {
    return missionTitle;
  }

  @JsonProperty("mission-title")
  public void setMissionTitle(String missionTitle) {
    this.missionTitle = missionTitle;
  }

  @JsonProperty("find-us-elsewhere")
  public String getFindUsElsewhere() {
    return findUsElsewhere;
  }

  @JsonProperty("find-us-elsewhere")
  public void setFindUsElsewhere(String findUsElsewhere) {
    this.findUsElsewhere = findUsElsewhere;
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
