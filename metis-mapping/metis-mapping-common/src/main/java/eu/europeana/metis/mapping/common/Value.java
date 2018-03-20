package eu.europeana.metis.mapping.common;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Value representation for validation
 * Created by ymamakis on 6/14/16.
 */
@Entity
@XmlRootElement
public class Value {

    @Id
    private ObjectId id;

    /**
     * The distinct value of a field
     */
    @Indexed
    private String value;

    @XmlElement
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
