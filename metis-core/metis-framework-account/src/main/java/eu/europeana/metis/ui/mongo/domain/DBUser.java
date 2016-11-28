package eu.europeana.metis.ui.mongo.domain;


import eu.europeana.metis.framework.common.Country;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Date;
import java.util.List;

/**
 * Mongo representation of a user with information not necessary to be stored in LDAP
 * Created by ymamakis on 11/24/16.
 */
@Entity
public class DBUser {

    @Id
    private ObjectId id;
    /**
     * The email of the user
     */
    @Indexed
    private String email;
    /**
     * The country of the user
     */
    private Country country;
    /**
     * The skypeId of the user
     */
    private String skypeId;
    /**
     * Whether the user is a Europeana Network member
     */
    private Boolean europeanaNetworkMember;
    /**
     * Notes with regards to the user
     */
    private String notes;
    /**
     * When the user was created
     */
    private Date created;
    /**
     * When the user was modified
     */
    private Date modified;
    /**
     * The list of all the available organizations
     */
    @Indexed
    private List<String> organizations;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getSkypeId() {
        return skypeId;
    }

    public void setSkypeId(String skypeId) {
        this.skypeId = skypeId;
    }

    public Boolean getEuropeanaNetworkMember() {
        return europeanaNetworkMember;
    }

    public void setEuropeanaNetworkMember(Boolean europeanaNetworkMember) {
        this.europeanaNetworkMember = europeanaNetworkMember;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date date) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }
}
