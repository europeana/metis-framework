package eu.europeana.metis.framework.organization;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by ymamakis on 3/2/16.
 */
@XmlRootElement
public class OrganizationList {

    private List<Organization> organizations;

    @XmlElement
    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }
}
