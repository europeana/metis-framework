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
import org.apache.commons.validator.routines.UrlValidator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Check whether a value is a URL
 * Created by ymamakis on 6/15/16.
 */
@Entity
@XmlRootElement
public class IsUrlFunction implements ValidationFunction {

    @Id
    private ObjectId id;

    private String type;
    /**
     * The id of the function
     * @return The id of the function
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of a function
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
        return "isUrlFunction";
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(String value) {
        UrlValidator validator = new UrlValidator();
        return validator.isValid(value);
    }

    @Override
    public String toString(){
        return "{\"isUrlFunction\":"+ id.toString()+"}";
    }
}
