package eu.europeana.metis.mapping.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 9/14/16.
 */
@Entity
@XmlRootElement
public class IsLanguageFunction implements ValidationFunction {
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
        return "isLanguageFunction";
    }

    @Override
    public boolean execute(String value) {
        return value.trim().length()==2;
    }
}
