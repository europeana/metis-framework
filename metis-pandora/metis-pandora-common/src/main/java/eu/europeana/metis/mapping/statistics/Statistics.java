package eu.europeana.metis.mapping.statistics;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * A representation of the field values for a field
 * Created by ymamakis on 6/14/16.
 */
@Entity
@XmlRootElement
public class Statistics {

    @Id
    private ObjectId id;
    private Set<StatisticsValue> values;
    @Indexed
    private String xpath;

    /**
     * The values for the field
     * @return The values for the field
     */
    @XmlElement
    public Set<StatisticsValue> getValues() {
        return values;
    }

    /**
     * The values for the field
     * @see StatisticsValue
     * @param values The values for the field
     */
    public void setValues(Set<StatisticsValue> values) {
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
}
