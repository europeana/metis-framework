package eu.europeana.metis.authentication.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.persistence.Column;
import javax.persistence.Entity;
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
  @Column(name = "salt")
  @JsonIgnore
  private byte[] salt;
  @Column(name = "password")
  @JsonIgnore
  private String password;
  @Column(name = "organization_id")
  private String organizationId;
  @Column(name = "organization_name")
  private String organizationName;
  @Column(name = "account_role")
  private String accountRole;
  @Column(name = "country")
  private String country;
  @Column(name = "skype_id")
  private String skypeId;
  @Column(name = "network_member")
  private boolean networkMember;
  @Column(name = "notes")
  private String notes;
  @Column(name = "active")
  private boolean active;
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
  }

  public MetisUser(JsonNode jsonNode) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Iterator<JsonNode> elements = jsonNode.elements();
    while (elements.hasNext()) {
      JsonNode next = elements.next();
      switch (next.get("val").textValue()) {
        case "CONTACTID":
          userId = next.get("content").textValue();
          break;
        case "First Name":
          firstName = next.get("content").textValue();
          break;
        case "Last Name":
          lastName = next.get("content").textValue();
          break;
        case "Email":
          email = next.get("content").textValue();
          break;
        case "Created Time":
          createdDate = dateFormat.parse(next.get("content").textValue());
          break;
        case "Modified Time":
          updatedDate = dateFormat.parse(next.get("content").textValue());
          break;
        case "Notes":
          notes = next.get("content").textValue();
          break;
        case "Skype Id":
          skypeId = next.get("content").textValue();
          break;
        case "Country":
          country = next.get("content").textValue();
          break;
        case "Network Member":
          networkMember = next.get("content").booleanValue();
          break;
        case "Active":
          active = next.get("content").booleanValue();
          break;
        case "Account Role":
          accountRole = next.get("content").textValue();
          break;
        case "Account Name": //This is actually the organization Name in Zoho
          organizationName = next.get("content").textValue();
          break;
      }
    }
  }

  public void setOrganizationIdFromJsonNode(JsonNode jsonNode) {
    Iterator<JsonNode> elements = jsonNode.elements();
    while (elements.hasNext()) {
      JsonNode next = elements.next();
      switch (next.get("val").textValue()) {
        case "ACCOUNTID":
          organizationId = next.get("content").textValue();
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

  public byte[] getSalt() {
    return salt;
  }

  public void setSalt(byte[] salt) {
    this.salt = salt;
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

  public String getAccountRole() {
    return accountRole;
  }

  public void setAccountRole(String accountRole) {
    this.accountRole = accountRole;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getSkypeId() {
    return skypeId;
  }

  public void setSkypeId(String skypeId) {
    this.skypeId = skypeId;
  }

  public boolean isNetworkMember() {
    return networkMember;
  }

  public void setNetworkMember(boolean networkMember) {
    this.networkMember = networkMember;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public MetisUserAccessToken getMetisUserAccessToken() {
    return metisUserAccessToken;
  }

  public void setMetisUserAccessToken(MetisUserAccessToken metisUserAccessToken) {
    this.metisUserAccessToken = metisUserAccessToken;
  }
}
