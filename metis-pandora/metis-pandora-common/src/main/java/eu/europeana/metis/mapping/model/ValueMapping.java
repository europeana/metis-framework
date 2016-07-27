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
package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A value mapping representation
 * Created by ymamakis on 4/13/16.
 */
@XmlRootElement
@Entity
public class ValueMapping {
    @Id
    private ObjectId id;
    private String key;
    private String value;

    /**
     * The key of the value mapping
     * @return The key of the value mapping
     */
    @XmlElement
    public String getKey() {
        return key;
    }

    /**
     * The key of the value mapping
     * @param key The key of the value mapping
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The value the key should b e replaced by
     * @return The value the key should be replaced by
     */
    @XmlElement
    public String getValue() {
        return value;
    }

    /**
     * Set the value the key should be replaced by
     * @param value the value the key should be replaced by
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The id of the Value Mapping
     * @return The id of the value mapping
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the value mapping
     * @param id The id of the mapping
     */
    public void setId(ObjectId id) {
        this.id = id;
    }
}
