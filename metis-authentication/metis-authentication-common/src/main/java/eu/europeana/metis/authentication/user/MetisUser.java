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

  /**
   * Constructor that converts a {@link MetisUserModel} class to a MetisUser.
   * @param metisUserModel the metis user model class
   */
  public MetisUser(MetisUserModel metisUserModel) {
    this.userId = metisUserModel.getUserId();
    this.email = metisUserModel.getEmail();
    this.firstName = metisUserModel.getFirstName();
    this.lastName = metisUserModel.getLastName();
    this.organizationId = metisUserModel.getOrganizationId();
    this.organizationName = metisUserModel.getOrganizationName();
    this.accountRole = metisUserModel.getAccountRole();
    this.country = metisUserModel.getCountry();
    this.networkMember = metisUserModel.isNetworkMember();
    this.metisUserFlag = metisUserModel.isMetisUserFlag();
    this.createdDate = metisUserModel.getCreatedDate();
    this.updatedDate = metisUserModel.getUpdatedDate();
    this.metisUserAccessToken = metisUserModel.getMetisUserAccessToken();
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
}
