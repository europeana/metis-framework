package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zoho.crm.library.crud.ZCRMRecord;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.zoho.ZohoConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.util.CollectionUtils;

/**
 * The Metis user containing all parameters.
 * <p>This class is a model class that is also used as a JPA class to the postgresql database</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Entity
@Table(name = "metis_users")
public class MetisUser {

  @Column(name = "user_id")
  private String userId;
  @Id
  @Column(name = "email")
  private String email;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  @Column(name = "password")
  @JsonIgnore
  private String password;
  @Column(name = "organization_id")
  private String organizationId;
  @Column(name = "organization_name")
  private String organizationName;
  @Column(name = "account_role")
  @Enumerated(EnumType.STRING)
  private AccountRole accountRole;
  @Column(name = "country")
  private String country;
  @Column(name = "network_member")
  private boolean networkMember;
  @Column(name = "metis_user")
  private boolean metisUserFlag;
  @Column(name = "created_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdDate;
  @Column(name = "updated_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date updatedDate;

  @OneToOne
  @JoinColumn(name = "email")
  private MetisUserAccessToken metisUserAccessToken;

  public MetisUser() {
    //Required for json serialization
  }

  public void checkZohoFieldsAndPopulate(ZCRMRecord zcrmRecord)
      throws ParseException, BadContentException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    HashMap<String, Object> zohoFields = zcrmRecord.getData();

    userId = Long.toString(zcrmRecord.getEntityId());
    firstName = (String) zohoFields.get(ZohoConstants.FIRST_NAME_FIELD);
    lastName = (String) zohoFields.get(ZohoConstants.LAST_NAME_FIELD);
    email = (String) zohoFields.get(ZohoConstants.EMAIL_FIELD);
    createdDate =
        zcrmRecord.getCreatedTime() == null ? null : dateFormat.parse(zcrmRecord.getCreatedTime());
    updatedDate = zcrmRecord.getModifiedTime() == null ? null
        : dateFormat.parse(zcrmRecord.getModifiedTime());
    country = (String) zohoFields.get(ZohoConstants.COUNTRY_FIELD);
    final List<String> participationLevel = (List<String>) (zohoFields
        .get(ZohoConstants.PARTICIPATION_LEVEL_FIELD));
    if (!CollectionUtils.isEmpty(participationLevel) && participationLevel
        .contains("Network Association Member")) {
      networkMember = true;
    }
    metisUserFlag =
        zohoFields.get(ZohoConstants.METIS_USER_FIELD) == null ? false
            : (Boolean) zohoFields.get(ZohoConstants.METIS_USER_FIELD);

    accountRole = AccountRole.getAccountRoleFromEnumName(
        (String) zohoFields.get(ZohoConstants.ACCOUNT_ROLE_FIELD));
    if (accountRole == AccountRole.METIS_ADMIN) {
      throw new BadContentException("Account Role in Zoho is not valid");
    }
    final ZCRMRecord accountName = (ZCRMRecord) zohoFields
        .get(ZohoConstants.ACCOUNT_NAME_FIELD);
    organizationId = Long.toString(accountName.getEntityId());
    organizationName = accountName.getLookupLabel();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  public AccountRole getAccountRole() {
    return accountRole;
  }

  public void setAccountRole(AccountRole accountRole) {
    this.accountRole = accountRole;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public boolean isNetworkMember() {
    return networkMember;
  }

  public void setNetworkMember(boolean networkMember) {
    this.networkMember = networkMember;
  }

  public boolean isMetisUserFlag() {
    return metisUserFlag;
  }

  public void setMetisUserFlag(boolean metisUserFlag) {
    this.metisUserFlag = metisUserFlag;
  }

  public Date getCreatedDate() {
    return createdDate == null ? null : new Date(createdDate.getTime());
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate == null ? null : new Date(createdDate.getTime());
  }

  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public MetisUserAccessToken getMetisUserAccessToken() {
    return metisUserAccessToken;
  }

  public void setMetisUserAccessToken(MetisUserAccessToken metisUserAccessToken) {
    this.metisUserAccessToken = metisUserAccessToken;
  }
}
