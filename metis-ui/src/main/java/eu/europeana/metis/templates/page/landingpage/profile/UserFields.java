package eu.europeana.metis.templates.page.landingpage.profile;

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
 * @since 2017-06-20
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "selectedOrganizations",
    "user_id",
    "user_first_name",
    "user_last_name",
    "user_email",
    "user_skype",
    "active",
    "approved",
    "created",
    "updated",
    "notes",
    "countries"
})
public class UserFields {

  @JsonProperty("selectedOrganizations")
  private SelectedOrganizations selectedOrganizations;
  @JsonProperty("user_id")
  private UserId userId;
  @JsonProperty("user_first_name")
  private UserFirstName userFirstName;
  @JsonProperty("user_last_name")
  private UserLastName userLastName;
  @JsonProperty("user_email")
  private UserEmail userEmail;
  @JsonProperty("user_skype")
  private UserSkype userSkype;
  @JsonProperty("active")
  private Active active;
  @JsonProperty("approved")
  private Approved approved;
  @JsonProperty("created")
  private Created created;
  @JsonProperty("updated")
  private Updated updated;
  @JsonProperty("notes")
  private Notes notes;
  @JsonProperty("countries")
  private Countries countries;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("selectedOrganizations")
  public SelectedOrganizations getSelectedOrganizations() {
    return selectedOrganizations;
  }

  @JsonProperty("selectedOrganizations")
  public void setSelectedOrganizations(SelectedOrganizations selectedOrganizations) {
    this.selectedOrganizations = selectedOrganizations;
  }

  @JsonProperty("user_id")
  public UserId getUserId() {
    return userId;
  }

  @JsonProperty("user_id")
  public void setUserId(UserId userId) {
    this.userId = userId;
  }

  @JsonProperty("user_first_name")
  public UserFirstName getUserFirstName() {
    return userFirstName;
  }

  @JsonProperty("user_first_name")
  public void setUserFirstName(UserFirstName userFirstName) {
    this.userFirstName = userFirstName;
  }

  @JsonProperty("user_last_name")
  public UserLastName getUserLastName() {
    return userLastName;
  }

  @JsonProperty("user_last_name")
  public void setUserLastName(UserLastName userLastName) {
    this.userLastName = userLastName;
  }

  @JsonProperty("user_email")
  public UserEmail getUserEmail() {
    return userEmail;
  }

  @JsonProperty("user_email")
  public void setUserEmail(UserEmail userEmail) {
    this.userEmail = userEmail;
  }

  @JsonProperty("user_skype")
  public UserSkype getUserSkype() {
    return userSkype;
  }

  @JsonProperty("user_skype")
  public void setUserSkype(UserSkype userSkype) {
    this.userSkype = userSkype;
  }

  @JsonProperty("active")
  public Active getActive() {
    return active;
  }

  @JsonProperty("active")
  public void setActive(Active active) {
    this.active = active;
  }

  @JsonProperty("approved")
  public Approved getApproved() {
    return approved;
  }

  @JsonProperty("approved")
  public void setApproved(Approved approved) {
    this.approved = approved;
  }

  @JsonProperty("created")
  public Created getCreated() {
    return created;
  }

  @JsonProperty("created")
  public void setCreated(Created created) {
    this.created = created;
  }

  @JsonProperty("updated")
  public Updated getUpdated() {
    return updated;
  }

  @JsonProperty("updated")
  public void setUpdated(Updated updated) {
    this.updated = updated;
  }

  @JsonProperty("notes")
  public Notes getNotes() {
    return notes;
  }

  @JsonProperty("notes")
  public void setNotes(Notes notes) {
    this.notes = notes;
  }

  @JsonProperty("countries")
  public Countries getCountries() {
    return countries;
  }

  @JsonProperty("countries")
  public void setCountries(Countries countries) {
    this.countries = countries;
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
