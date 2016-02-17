package eu.europeana.metis.framework.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class HttpMetadata extends HarvestingMetadata{
    @XmlElement
    private String harvestUser;

    @XmlElement
    private String harvestPassword;

    public String getHarvestUser() {
        return harvestUser;
    }

    public void setHarvestUser(String harvestUser) {
        this.harvestUser = harvestUser;
    }

    public String getHarvestPassword() {
        return harvestPassword;
    }

    public void setHarvestPassword(String harvestPassword) {
        this.harvestPassword = harvestPassword;
    }
}
