/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.metis.core.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
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
    @JsonSerialize(using = ObjectIdSerializer.class)
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
