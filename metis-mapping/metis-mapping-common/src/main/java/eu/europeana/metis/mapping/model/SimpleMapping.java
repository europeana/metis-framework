package eu.europeana.metis.mapping.model;

import eu.europeana.metis.mapping.validation.Flag;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple mapping (no conditions) java object
 * Created by gmamakis on 8-4-16.
 */
@XmlRootElement
@Entity
public class SimpleMapping implements IMapping{
    private MappingType type = MappingType.EMPTY;
    @Embedded
    private Function function;
    private String sourceField;
    @Embedded
    private ValueMappings valueMappings;
    private String constant;
    private String parameter;
    private Flag flag;
    @Id
    private ObjectId id;

    /**
     * The id of the mapping
     * @return the id of the mapping
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the mapping
     * @param id The id of the mapping
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * {@inheritDoc IMapping {@link #getType()}}
     */
    @XmlElement
    public MappingType getType() {
        return type;
    }

    /**
     * Set the mapping type
     * @param type Set the mapping type
     */
    public void setType(MappingType type) {
        this.type = type;
    }

    /**
     * {@inheritDoc IMapping {@link #getFunction()} }
     */
    @XmlElement
    public Function getFunction() {
        return function;
    }

    /**
     * Set the functions of the mapping
     * @see Function
     * @param function Set the functions of the simple field mapping
     */
    public void setFunction(Function function) {
        this.function = function;
    }

    /**
     * The XPath of the source field
     * @return The XPath of the source field
     */
    @XmlElement
    public String getSourceField() {
        return sourceField;
    }

    /**
     * Set the XPath of the source field
     * @param sourceField The XPath of the source field
     */
    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    /**
     * {@inheritDoc IMapping {@link #getValueMappings()} }
     */
    @XmlElement
    public ValueMappings getValueMappings() {
        return valueMappings;
    }

    public void setValueMappings(ValueMappings valueMappings) {
        this.valueMappings = valueMappings;
    }

    /**
     * {@inheritDoc IMapping {@link #getConstant()}}
     */
    @XmlElement
    public String getConstant() {
        return constant;
    }

    /**
     * Set the constant value to be assigned to field
     * @param constant The constant value to be assigned on a field
     */
    public void setConstant(String constant) {
        this.constant = constant;
    }

    /**
     * {@inheritDoc IMapping {@link #getParameter()}}
     */
    @XmlElement
    public String getParameter() {
        return parameter;
    }

    /**
     * Set a parameter (variable) for the field mapping
     * @param parameter The parameter to set
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * {@inheritDoc IMapping {@link #getFlag()} }
     */
    @XmlElement
    public Flag getFlag() {
        return flag;
    }

    /**
     * Set a flag to mapping specifying that it is suspicious or blocker
     * @param flag The flag to set
     */
    public void setFlag(Flag flag) {
        this.flag = flag;
    }
}
