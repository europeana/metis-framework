package eu.europeana.metis.ui.mongo.domain;

import org.mongodb.morphia.annotations.Entity;

/**
 * Created by ymamakis on 10-1-17.
 */
@Entity
public class OrganizationRole {

    private Roles role;
    private String organizationId;

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
