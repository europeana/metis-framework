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
package eu.europeana.metis.dereference;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An original Entity as downloaded from an online RDF repository
 * Created by ymamakis on 2/11/16.
 */
@XmlRootElement
@Entity("OriginalEntity")
public class OriginalEntity {

    @Id
    private String id;
    /**
     * The URI it was downloaded from
     */
    @Indexed(unique = true)
    private String URI;

    /**
     * The RDF/XML that describes the entity
     */
    private String xml;
    @XmlElement
    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }
    @XmlElement
    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
