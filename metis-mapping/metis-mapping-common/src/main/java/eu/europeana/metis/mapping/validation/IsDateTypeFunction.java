package eu.europeana.metis.mapping.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ymamakis on 9/15/16.
 */
@Entity
@XmlRootElement
public class IsDateTypeFunction implements ValidationFunction {
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
        return "isDateTypeFunction";
    }

    @Override
    public boolean execute(String value) {
        Pattern pattern = Pattern.compile("[0-9]{4}-[0-1]{1}[0-9]{1}-[0-3$]{1}[0-9$]{1}[Z$]?[\\+\\-]?[0-9]{0,2}[\\:]*[0-9$]{0,2}");
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}
