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
    @Indexed
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
}
