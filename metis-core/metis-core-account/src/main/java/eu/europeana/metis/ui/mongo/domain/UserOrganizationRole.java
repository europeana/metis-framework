package eu.europeana.metis.ui.mongo.domain;

import org.mongodb.morphia.annotations.Entity;

/**
 * Created by ymamakis on 10-1-17.
 */
@Entity
public class UserOrganizationRole {

  private Role role;
  private String organizationId;
  private String organizationName;

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
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
}
