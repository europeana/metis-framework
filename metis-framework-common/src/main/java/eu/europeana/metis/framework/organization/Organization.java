package eu.europeana.metis.framework.organization;


import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.dataset.Dataset;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Date;
import java.util.List;

/**
 * The Organization representation in METIS
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
@Entity
public class Organization {

    /**
     * Id
     */
    @Id
    private ObjectId id;


    /**
     * The organization ID
     */
    @Indexed (unique = true)
    private String organizationId;

    /**
     * The organization URI from the CRM
     */
    private String organizationUri;
    /**
     * The harvesting metadata that are applicable for all the datasets of the organization
     * Override that to specify dataset specific ones
     */
    private HarvestingMetadata harvestingMetadata;

    /**
     * The name of the organization
     */
    private String name;

    /**
     * Created
     */
    private Date created;

    /**
     * Updated
     */
    private Date modified;

    /**
     * Acronym
     */
    private String acronym;

    /**
     * Role
     */
    private List<String> roles;

    /**
     * The datasets associated with the organization
     */
    @Reference
    private List<Dataset> datasets;
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
    @XmlElement
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    @XmlElement
    public String getOrganizationUri() {
        return organizationUri;
    }

    public void setOrganizationUri(String organizationUri) {
        this.organizationUri = organizationUri;
    }
    @XmlElement
    public HarvestingMetadata getHarvestingMetadata() {
        return harvestingMetadata;
    }

    public void setHarvestingMetadata(HarvestingMetadata harvestingMetadata) {
        this.harvestingMetadata = harvestingMetadata;
    }
    @XmlElement
    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @XmlElement
    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @XmlElement
    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    @XmlElement
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
