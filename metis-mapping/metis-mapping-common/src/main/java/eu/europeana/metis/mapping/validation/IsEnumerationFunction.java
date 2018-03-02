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
