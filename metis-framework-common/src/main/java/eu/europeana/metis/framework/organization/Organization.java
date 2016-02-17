package eu.europeana.metis.framework.organization;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Reference;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.dataset.Dataset;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class Organization {

    @Id
    private String id;

    @XmlElement
    @Indexed
    private String organizationId;

    @XmlElement
    private String orgnaizationUri;

    @XmlElement
    private HarvestingMetadata harvestingMetadata;

    @XmlElement
    @Reference
    private List<Dataset> datasets;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrgnaizationUri() {
        return orgnaizationUri;
    }

    public void setOrgnaizationUri(String orgnaizationUri) {
        this.orgnaizationUri = orgnaizationUri;
    }

    public HarvestingMetadata getHarvestingMetadata() {
        return harvestingMetadata;
    }

    public void setHarvestingMetadata(HarvestingMetadata harvestingMetadata) {
        this.harvestingMetadata = harvestingMetadata;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }
}
