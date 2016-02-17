package eu.europeana.metis.framework.common;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by ymamakis on 2/17/16.
 */

public class HarvestingMetadata {
    @XmlElement
    private HarvestType harvestType=null;
    @XmlElement
    private String metadataSchema =null;

    public HarvestType getHarvestType(){
        return harvestType;
    }

    public void setHarvestType(HarvestType hType){
        harvestType = hType;
    }
}
