package eu.europeana.metis.framework.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class OAIMetadata extends HarvestingMetadata {
    @XmlElement
    private String harvestUrl;
    @XmlElement
    private String metadataFormat;

    public String getHarvestUrl() {
        return harvestUrl;
    }

    public void setHarvestUrl(String harvestUrl) {
        this.harvestUrl = harvestUrl;
    }

    public String getMetadataFormat() {
        return metadataFormat;
    }

    public void setMetadataFormat(String metadataFormat) {
        this.metadataFormat = metadataFormat;
    }
}
