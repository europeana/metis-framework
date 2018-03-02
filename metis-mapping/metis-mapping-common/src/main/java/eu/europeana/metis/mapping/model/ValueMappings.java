package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * A set of value mappings. In order for the value mappings to operate, one must specify for a field (key),
 * every value mapping (key [to be replaces]-value), even the ones that the key and the value will be the same.
 * Created by ymamakis on 4/13/16.
 */
@XmlRootElement
@Entity
public class ValueMappings {
    @Id
    private ObjectId id;
    @Embedded
    private List<ValueMapping> mappings;
    private String key;
    private String index;

    /**
     * A list of all the possible value mappings for a field. Specify even the ones that do not result
     * in a change between a key and a value
     * @return The list of all the value mappings
     */
    @XmlElement
    public List<ValueMapping> getMappings() {
        return mappings;
    }

    /**
     * Specify list of Value mappings
     * @param mappings The list of value mappings
     */
    public void setMappings(List<ValueMapping> mappings) {
        this.mappings = mappings;
    }

    /**
     * Specify index for the variable to be generated from the value mapping
     * @return The index of the variable
     */
    @XmlElement
    public String getIndex() {
        return index;
    }

    /**
     * The index of the resulting variable in XSL
     * @param index The index of the variable in the resulting XSL
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * The name of the variable
     * @return The name of the variable
     */
    @XmlElement
    public String getKey() {
        return key;
    }

    /**
     * The name of the variable
     * @param key The name of the variable
     */
    public void setKey(String key) {
        this.key = key;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
