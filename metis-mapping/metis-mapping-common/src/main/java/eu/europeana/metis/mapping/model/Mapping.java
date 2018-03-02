package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Main class representing an Schema into a Java object.
 * This class can contain a template (empty mapping),
 * a crosswalk (pre-populated mapping) or specific dataset
 * mappings
 *
 * Created by ymamakis on 4/7/16.
 */
@Entity
@XmlRootElement
public class Mapping {

    @Id
    private ObjectId objId;
    @Indexed(unique = true)
    private String name;
    private Date creationDate;
    private Date lastModified;
    @Indexed(unique = false)
    private String organization;
    @Indexed(unique = false)
    private String dataset;
    @Reference
    private Mappings mappings;
    private String xsl;
    @Reference
    private MappingSchema targetSchema;
    private Map<String,String> parameters;

    private Set<String> schematronRules;

    /**
     * The id of the mapping
     * @return The id of the mapping
     */
    @XmlElement
    public ObjectId getObjId() {
        return objId;
    }

    /**
     * Set the id of the mapping
     * @param objId The id of the mapping
     */
    public void setObjId(ObjectId objId) {
        this.objId = objId;
    }

    /**
     * The name of the mapping
     * @return The name of the mapping
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * Set the name of the mapping
     * @param name the name of the mapping
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Creation date of the mapping
     * @return Creation date of the mapping
     */
    @XmlElement
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Creation date of the mapping
     * @param creationDate Creation date of the mapping
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Modification date for the mapping
     * @return modification date for the mapping
     */
    @XmlElement
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Modification date for the mapping
     * @param lastModified Modification date for the mapping
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * The organization name for the mapping. It can be empty for crosswalks and templates
     * but must be filled in for populated mappings after use.
     * @return The organization name for the mapping
     */
    @XmlElement
    public String getOrganization() {
        return organization;
    }

    /**
     * The organization name for the mapping.
     * @param organization the organization name for the mappinf
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * The dataset name for the mapping.  It can be empty for crosswalks and templates
     * but must be filled in for populated mappings after use
     * @return the dataset name of the mapping
     */
    @XmlElement
    public String getDataset() {
        return dataset;
    }

    /**
     * The dataset name for the mapping
     * @param dataset The dataset mapping for the mapping
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    /**
     * The resulting XSL after the mapping has been converted. Can be empty for templates
     * but not for crosswalks or other populated mappings. It is here as convenience in order not to
     * regenerate the everything from the beginning
     * @return The XSL serialization of the Mapping
     */
    @XmlElement
    public String getXsl() {
        return xsl;
    }

    /**
     * Set the xsl for the mapping
     * @param xsl The xsl for the mapping
     */
    public void setXsl(String xsl) {
        this.xsl = xsl;
    }

    /**
     * The target schema for the mapping
     * @see MappingSchema
     * @return The target schema for the mapping
     */
    @XmlElement
    public MappingSchema getTargetSchema() {
        return targetSchema;
    }

    /**
     * The target schema for the mapping
     * @see MappingSchema
     * @param targetSchema The target schema for the mapping
     */
    public void setTargetSchema(MappingSchema targetSchema) {
        this.targetSchema = targetSchema;
    }

    /**
     * The mappings of {@link Element} and {@link Attribute} for this mapping
     * @see Mappings
     * @return The mappings of {@link Element} and {@link Element} for this mapping
     */
    @XmlElement
    public Mappings getMappings() {
        return mappings;
    }

    /**
     * Set the mappings of fields for this mapping
     * @see Mappings
     * @param mappings The mappings of fields
     */
    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    /**
     * Get the parameters and their values for this mapping. These exist as a key-value pair and define variables
     * @return The parameters and their values for this mapping
     */
    @XmlElement
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * The parameters and their values for this mapping
     * @param parameters The parameters and their values for this mapping
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @XmlElement
    public Set<String> getSchematronRules() {
        return schematronRules;
    }

    public void setSchematronRules(Set<String> schematronRules) {
        this.schematronRules = schematronRules;
    }
}
