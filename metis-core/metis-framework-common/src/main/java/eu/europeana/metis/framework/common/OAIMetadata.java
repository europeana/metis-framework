package eu.europeana.metis.framework.common;


import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * OAIPMH specific technical metadata
 * Created by ymamakis on 2/17/16.
 */
@Entity
@XmlRootElement
public class OAIMetadata extends HarvestingMetadata {

    /**
     * The harvesting URL
     */
    private String harvestUrl;

    /**
     * The metadata format
     */
    private String metadataFormat;
    @XmlElement
    public String getHarvestUrl() {
        return harvestUrl;
    }

    public void setHarvestUrl(String harvestUrl) {
        this.harvestUrl = harvestUrl;
    }
    @XmlElement
    public String getMetadataFormat() {
        return metadataFormat;
    }

    public void setMetadataFormat(String metadataFormat) {
        this.metadataFormat = metadataFormat;
    }
}
