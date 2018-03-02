package eu.europeana.metis.mapping.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Check whether a field is a URI
 * Created by ymamakis on 6/15/16.
 */
@Entity
@XmlRootElement
public class IsUriFunction implements ValidationFunction {
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

    /**
     * {@inheritDoc}
     */
    public boolean execute(String value) {
        try {
            URI uri = new URI(value);
            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void setType(String type){
        this.type = type;
    }

    //@XmlElement
    @Override
    @JsonIgnore
    public String getType() {
        return "isUriFunction";
    }
}
