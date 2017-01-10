package eu.europeana.metis.ui.mongo.domain;

/**
 * Created by ymamakis on 10-1-17.
 */
public class OrganizationRole {

    private String role;
    private String organizationId;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
