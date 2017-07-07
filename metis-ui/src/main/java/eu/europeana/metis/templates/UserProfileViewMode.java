package eu.europeana.metis.templates;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by erikkonijnenburg on 07/07/2017.
 */
public class UserProfileViewMode {
  @JsonProperty("value")
  private String viewMode;

  public UserProfileViewMode() {
  }

  public UserProfileViewMode(String viewMode) {
    this.viewMode = viewMode;
  }
  @JsonProperty("value")

  public void setViewMode(String viewMode) {
    this.viewMode = viewMode;
  }
  @JsonProperty("value")
  public String getViewMode() {
    return viewMode;
  }
}
