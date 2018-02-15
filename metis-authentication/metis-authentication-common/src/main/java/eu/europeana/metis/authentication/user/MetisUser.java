package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.metis.exception.BadContentException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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

/**
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

  /**
   * MetisUser for the Metis project.
   *
   * @param jsonNode the {@link JsonNode} to construct the MetisUser
   * @throws ParseException if the content is unparsable
   * @throws BadContentException if the content of the JsonNode is unacceptable because of rules that should be followed
   */
  public MetisUser(JsonNode jsonNode) throws ParseException, BadContentException {
    parseJsonNodeZohoUserToMetisUser(jsonNode);
  }

  private void parseJsonNodeZohoUserToMetisUser(JsonNode jsonNode)
      throws BadContentException, ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Iterator<JsonNode> elements = jsonNode.elements();
    while (elements.hasNext()) {
      JsonNode next = elements.next();
      JsonNode val = next.get("val");
      JsonNode content = next.get("content");
      switch (val.textValue()) {
        case "CONTACTID":
          userId = content.textValue();
          break;
        case "First Name":
          firstName = content.textValue();
          break;
        case "Last Name":
          lastName = content.textValue();
          break;
        case "Email":
          email = content.textValue();
          break;
        case "Created Time":
          createdDate = dateFormat.parse(content.textValue());
          break;
        case "Modified Time":
          updatedDate = dateFormat.parse(content.textValue());
          break;
        case "Country":
          country = content.textValue();
          break;
        case "Participation level":
          if (content.textValue().contains("Network Association Member")) {
            networkMember = true;
          }
          break;
        case "Metis user":
          metisUserFlag = Boolean.parseBoolean(content.textValue());
          break;
        case "Account Role":
          accountRole = AccountRole.getAccountRoleFromEnumName(content.textValue());
          if (accountRole == AccountRole.METIS_ADMIN) {
            throw new BadContentException("Account Role in Zoho is not valid");
          }
          break;
        case "Account Name": //This is actually the organization Name in Zoho
          organizationName = content.textValue();
          break;
        default:
          break;

      }
    }
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
