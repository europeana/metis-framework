package eu.europeana.metis.authentication.user;

import java.util.Date;

/**
 * The Metis user containing all parameters.
 * <p>This class is a model class</p>
 */
public class MetisUser {

  private String userId;
  private String email;
  private String firstName;
  private String lastName;
  private String organizationId;
  private String organizationName;
  private AccountRole accountRole;
  private String country;
  private boolean networkMember;
  private boolean metisUserFlag;
  private Date createdDate;
  private Date updatedDate;
  private MetisUserAccessToken metisUserAccessToken;

  public MetisUser() {
    //Required for json serialization
  }

  public MetisUser(MetisUserModel record) {
    this.userId = record.getUserId();
    this.email = record.getEmail();
    this.firstName = record.getFirstName();
    this.lastName = record.getLastName();
    this.organizationId = record.getOrganizationId();
    this.organizationName = record.getOrganizationName();
    this.accountRole = record.getAccountRole();
    this.country = record.getCountry();
    this.networkMember = record.isNetworkMember();
    this.metisUserFlag = record.isMetisUserFlag();
    this.createdDate = record.getCreatedDate();
    this.updatedDate = record.getUpdatedDate();
    this.metisUserAccessToken = record.getMetisUserAccessToken();
  }

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public AccountRole getAccountRole() {
    return accountRole;
  }

  public String getCountry() {
    return country;
  }

  public boolean isNetworkMember() {
    return networkMember;
  }

  public boolean isMetisUserFlag() {
    return metisUserFlag;
  }

  public Date getCreatedDate() {
    return createdDate == null ? null : new Date(createdDate.getTime());
  }

  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public MetisUserAccessToken getMetisUserAccessToken() {
    return metisUserAccessToken;
  }

  public void setMetisUserAccessToken(MetisUserAccessToken metisUserAccessToken) {
    this.metisUserAccessToken = metisUserAccessToken;
  }
}
