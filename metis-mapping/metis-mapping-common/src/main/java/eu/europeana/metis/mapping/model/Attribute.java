package eu.europeana.metis.mapping.model;

import eu.europeana.metis.mapping.validation.Flag;
import eu.europeana.metis.mapping.validation.ValidationRule;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Definition of a target Attribute. This class is extended by
 * the Element class
 *  @see Element
 * Created by ymamakis on 4/7/16.
 */
@Entity
@XmlRootElement
public class Attribute {
    @Id
    private ObjectId id;
    private String namespace;
    private String prefix;
    private String name;
    @Indexed
    private String xPathFromRoot;
    private String description;
    private List<String> enumerations;
    private boolean mandatory;
    @Embedded
    private List<SimpleMapping> mappings;
    private int minOccurs;
    private int maxOccurs;
    @Embedded
    private List<ConditionMapping> conditionalMappings;
    private String annotations;

    @Embedded
    private List<ValidationRule> rules;

    @Embedded
    private List<Flag> flags;

    /**
     * The minumum occurence of an attribute (typically 0-1)
     * @return The minimum occurence of an xml field according to its schema definition
     */
    @XmlElement
    public int getMinOccurs() {
        return minOccurs;
    }

    /**
     * Set the minimum number of occurences
     * @param minOccurs The minimum number of a field occurence
     */
    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Specify the maximum number of occurences (cardinality) for a field
     * @return The maximum number of occurrences (typically 1 or -1 for unbounded)
     */
    public int getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Set the maximum number of occurrences for a field
     * @param maxOccurs The maximum numbers of occurrences
     */
    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * List of simple (non conditional) mappings for the given field
     * @return The list of simple (non-conditional) mappings for the field
     */
    @XmlElement
    public List<SimpleMapping> getMappings() {
        return mappings;
    }

    /**
     * Set the list of simple mappings for the field
     * @param mappings The list of simple (non-conditional) mappings
     */
    public void setMappings(List<SimpleMapping> mappings) {
        this.mappings = mappings;
    }

    /**
     * The namespace of the field
     * @return The namespace of the field
     */
    @XmlElement
    public String getNamespace() {
        return namespace;
    }

    /**
     * Set the namespace of the field
     * @param namespace The namespace of the field
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * The prefix of the field
     * @return The profix of the field
     */
    @XmlElement
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix for the field
     * @param prefix The prefix for the field
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * The name of the field
     * @return The name of the field
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * Set the name for the field
     * @param name The name for the field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list of conditional mappings for a field
     * @return The list of conditional mappings
     */
    @XmlElement
    public List<ConditionMapping> getConditionalMappings() {
        return conditionalMappings;
    }

    /**
     * Set the list of conditional mappings
     * @param conditionalMappings The list of conditional mappings
     *
     */
    public void setConditionalMappings(List<ConditionMapping> conditionalMappings) {
        this.conditionalMappings = conditionalMappings;
    }

    /**
     * Get the full XPath form the root of the document
     * @return The full XPath from the root of the document
     */
    @XmlElement
    public String getxPathFromRoot() {
        return xPathFromRoot;
    }

    /**
     * The XPath from the root
     * @param xPathFromRoot The XPath from the root of the document
     */
    public void setxPathFromRoot(String xPathFromRoot) {
        this.xPathFromRoot = xPathFromRoot;
    }

    /**
     * Get the description of the field
     * @return The description of the field
     */
    @XmlElement
    public String getDescription() {
        return description;
    }

    /**
     * SEt the description of the field
     * @param description The description of the field
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * List of allowed values for the field
     * @return The list of allowed values of the field according to the schema definition
     */
    @XmlElement
    public List<String> getEnumerations() {
        return enumerations;
    }

    /**
     * Set the list off allowed values according to the field definition
     * @param enumerations The list of allowed values
     */
    public void setEnumerations(List<String> enumerations) {
        this.enumerations = enumerations;
    }

    /**
     * Flag to indicate whether the field is mandatory or not
     * @return whether the field is mandatory or not
     */
    @XmlElement
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Set the field as mondatory
     * @param mandatory The flag whether the field is mandatory
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * The id of the field
     * @return The id of the field
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * Set the id of the field
     * @param id The id of the field
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * The annotations (documentation) for the field
     * @return The annotations of the field
     */
    @XmlElement
    public String getAnnotations() {
        return annotations;
    }

    /**
     * Set the annotatios for the field
     * @param annotations The annotations for the field
     */
    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    /**
     * Rules set for validation of the field. These rules will generate flags for the
     * user to notify them that something is suspicious or wrong
     * @return The list of validation rules
     */
    @XmlElement
    public List<ValidationRule> getRules() {
        return rules;
    }

    /**
     * Set the list of validation rules
     * @param rules The list of validation rules
     */
    public void setRules(List<ValidationRule> rules) {
        this.rules = rules;
    }

    /**
     * A list of flags (blocker and susicious values) that need user input
     * @return The list of flags for the generated mapping
     */
    @XmlElement
    public List<Flag> getFlags() {
        return flags;
    }

    /**
     * Set the list of flags
     * @param flags The list of flags
     */
    public void setFlags(List<Flag> flags) {
        this.flags = flags;
    }
}
