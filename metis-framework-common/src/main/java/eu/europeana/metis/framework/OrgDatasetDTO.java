package eu.europeana.metis.framework;

import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.organization.Organization;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 3/2/16.
 */
@XmlRootElement
public class OrgDatasetDTO {
    private Organization organization;
    private Dataset dataset;

    @XmlElement
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @XmlElement
    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
}
