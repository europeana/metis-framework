package eu.europeana.metis.framework.common;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;

/**
 * Basic technical metadata for harvesting
 * Created by ymamakis on 2/17/16.
 */
@Entity
public class HarvestingMetadata {

    @Id
    private ObjectId id;
    /**
     * Harvesting type (controlled list)
     */
    private HarvestType harvestType;

    /**
     * The schema the metadata conform to
     */
    private String metadataSchema;
    @XmlElement
    public HarvestType getHarvestType(){
        return harvestType;
    }

    public void setHarvestType(HarvestType hType){
        harvestType = hType;
    }
    @XmlElement
    public String getMetadataSchema() {
        return metadataSchema;
    }

    public void setMetadataSchema(String metadataSchema) {
        this.metadataSchema = metadataSchema;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
