package eu.europeana.metis.framework.common;


import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HTTP specific metadata
 * Created by ymamakis on 2/17/16.
 */
@Entity
@XmlRootElement
public class HttpMetadata extends HarvestingMetadata{

    /**
     * The harvest username
     */
    private String harvestUser;

    /**
     * The harvest password
     */
    private String harvestPassword;
    @XmlElement
    public String getHarvestUser() {
        return harvestUser;
    }

    public void setHarvestUser(String harvestUser) {
        this.harvestUser = harvestUser;
    }
    @XmlElement
    public String getHarvestPassword() {
        return harvestPassword;
    }

    public void setHarvestPassword(String harvestPassword) {
        this.harvestPassword = harvestPassword;
    }
}
