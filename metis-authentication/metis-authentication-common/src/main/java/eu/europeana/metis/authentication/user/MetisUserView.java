package eu.europeana.metis.authentication.user;

import java.util.Date;

/**
 * The Metis user containing all parameters.
 * <p>This class is a model class</p>
 */
public class MetisUserView {

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

  public MetisUserView() {
    //Required for json serialization
  }

  /**
   * Constructor that converts a {@link MetisUser} class to a MetisUserView.
   * @param metisUser the metis user model class
   */
  public MetisUserView(MetisUser metisUser) {
    this.userId = metisUser.getUserId();
    this.email = metisUser.getEmail();
    this.firstName = metisUser.getFirstName();
    this.lastName = metisUser.getLastName();
    this.organizationId = metisUser.getOrganizationId();
    this.organizationName = metisUser.getOrganizationName();
    this.accountRole = metisUser.getAccountRole();
    this.country = metisUser.getCountry();
    this.networkMember = metisUser.isNetworkMember();
    this.metisUserFlag = metisUser.isMetisUserFlag();
    this.createdDate = metisUser.getCreatedDate();
    this.updatedDate = metisUser.getUpdatedDate();
    this.metisUserAccessToken = metisUser.getMetisUserAccessToken();
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
