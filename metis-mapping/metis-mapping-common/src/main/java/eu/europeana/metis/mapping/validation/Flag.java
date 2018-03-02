package eu.europeana.metis.mapping.validation;

import eu.europeana.metis.mapping.common.Value;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A flag notifying the user of suspicious or blocker values after a mapping has been created.
 * The flag is specific for a value, field (element or attribute), a dataset and a mapping.
 * One can create automatic flags based on {@link ValidationRule} or manually flag  a value
 * as blocker or suspicious
 * Created by ymamakis on 6/9/16.
 */
@Entity
@XmlRootElement
public class Flag {

    @Id
    private ObjectId id;
    private String message;
    private FlagType flagType;
    @Indexed
    private Value value;
    @Indexed
    private String xPath;
    @Indexed
    private String mappingId;

    /**
     * The message of the flag
     *
     * @return The message
     */
    @XmlElement
    public String getMessage() {
        return message;
    }

    /**
     * Set the message of a flag
     *
     * @param message The message of a flag
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the flag type (OK,WARNING,BLOCKER)
     *
     * @return the falg type
     */
    @XmlElement
    public FlagType getFlagType() {
        return flagType;
    }

    /**
     * Set the flag type
     *
     * @param flagType The flag type
     */
    public void setFlagType(FlagType flagType) {
        this.flagType = flagType;
    }

    /**
     * The id of the flag
     *
     * @return The id of the flag
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the flag
     *
     * @param id The id of the flag
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * The value for which the flag applies
     *
     * @return The value for which the flag applies
     */
    @XmlElement
    public Value getValue() {
        return value;
    }

    /**
     * Set the flag for which the value applies
     *
     * @param value The flag for which the value applies
     */
    public void setValue(Value value) {
        this.value = value;
    }

    /**
     * The XPath of the element that created the flag
     *
     * @return The XPath of the element for which the flag applies (roginal dataset)
     */
    @XmlElement
    public String getxPath() {
        return xPath;
    }

    /**
     * The xpath of the element that generated the flg
     *
     * @param xPath The xpath of the element that generated the flag
     */
    public void setxPath(String xPath) {
        this.xPath = xPath;
    }

    /**
     * The mapping id for which the flag is valid
     *
     * @return The mapping id for which the flag is valid
     */
    @XmlElement
    public String getMappingId() {
        return mappingId;
    }

    /**
     * The mapping id for which the flag is valid
     *
     * @param mappingId
     */
    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }
}
