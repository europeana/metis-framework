package eu.europeana.metis.common;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-21
 */
public class UpdateUserProfileRequest {

  private String firstName;
  private String lastName;
  private String email;
  private String country;
  private String notes;
  private String skype;

  public String getUserFirstName() {
    return firstName;
  }

  public void setUserFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getUserLastName() {
    return lastName;
  }

  public void setUserLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserSkype() {
    return skype;
  }

  public void setUserSkype(String skype) {
    this.skype = skype;
  }

//  public String getUserEmail() {
//    return email;
//  }
//
//  public void setUserEmail(String email) {
//    this.email = email;
//  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getNotes() { return notes; }

  public void setNotes(String notes) {
    this.notes = notes;
  }

}
