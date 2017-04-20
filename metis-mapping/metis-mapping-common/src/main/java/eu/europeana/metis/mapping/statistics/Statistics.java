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
package eu.europeana.metis.mapping.statistics;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * A representation of the field values for a field
 * Created by ymamakis on 6/14/16.
 */
@Entity
@XmlRootElement
public class Statistics {

    @Id
    private ObjectId id;
    private List<StatisticsValue> values;
    @Indexed
    private String datasetId;
    @Indexed
    private String xpath;

    /**
     * The values for the field
     * @return The values for the field
     */
    @XmlElement
    public List<StatisticsValue> getValues() {
        return values;
    }

    /**
     * The values for the field
     * @see StatisticsValue
     * @param values The values for the field
     */
    public void setValues(List<StatisticsValue> values) {
        this.values = values;
    }

    /**
     * The id of the representation
     * @return the id of the representation
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the representation
     * @param id The id of the representation
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * The XPath of the field
     * @return The XPath of the field
     */
    @XmlElement
    public String getXpath() {
        return xpath;
    }

    /**
     * The id of the XPath
     * @param xpath The id of the XPath
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }


    @XmlElement
    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
