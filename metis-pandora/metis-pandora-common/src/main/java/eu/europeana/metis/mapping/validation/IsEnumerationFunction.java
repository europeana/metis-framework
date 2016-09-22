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
package eu.europeana.metis.mapping.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Check whether a value exists in an enumeration
 * Created by ymamakis on 6/15/16.
 */
@Entity
@XmlRootElement
public class IsEnumerationFunction implements ValidationFunction{

    @Id
    private ObjectId id;
    private Set<String> values;
    private String type;

    /**
     * {@inheritDoc}
     */
    public boolean execute(String value) {
        return values.contains(value);
    }

    /**
     * Set the values against which to check the given value
     * @param values Th
     */
    public void setValues(Set<String> values){
        this.values = values;
    }

    /**
     * The enumeration for the specfic field
     * @return
     */
    @XmlElement
    public Set<String> getValues(){
        return values;
    }

    /**
     * The id of the function
     * @return The id of the function
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the function
     * @param id The id of the function
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setType(String type){
        this.type = type;
    }

    //@XmlElement
    @Override
    @JsonIgnore
    public String getType() {
        return "isEnumerationFunction";
    }
}
