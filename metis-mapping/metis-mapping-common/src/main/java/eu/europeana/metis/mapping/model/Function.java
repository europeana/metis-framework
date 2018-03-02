package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The definition of an XSLT function
 * Created by ymamakis on 4/7/16.
 */
@Entity
@XmlRootElement(name = "func")
public class Function {

    @Id
    private ObjectId id;
    private FunctionType type;

    private String[] arguments;

    /**
     * The type of Function
     * @see FunctionType
     * @return The type of Function
     */
    @XmlElement(name="call")
    public FunctionType getType() {
        return type;
    }

    /**
     * Set the type of Function
     * @param type The type of Function
     */
    public void setType(FunctionType type) {
        this.type = type;
    }

    /**
     * An array of arguments for the function
     * @return The array of arguments for the function
     */
    @XmlElement(name="arguments")
    public String[] getArguments() {
        return arguments;
    }

    /**
     * The array of arguments for the function
     * @param arguments The array of arguments for the function
     */
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
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
}
