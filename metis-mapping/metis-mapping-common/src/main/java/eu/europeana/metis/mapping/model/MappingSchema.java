package eu.europeana.metis.mapping.model;

import eu.europeana.validation.model.Schema;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.Set;

/**
 * A Mapping Schema representation
 * Created by ymamakis on 4/7/16.
 */
@Entity
@XmlRootElement
public class MappingSchema extends Schema {



    @Embedded
    private XPathHolder rootPath;
    private Set<String> mandatoryXpath;
    private Set<String> schematronRules;
    private Map<String,String> documentation;


    /**
     * The root XPath
     * @see XPathHolder
     * @return The Xpath root
     */
    @XmlElement
    public XPathHolder getRootPath() {
        return rootPath;
    }

    /**
     * Set the root XPath
     * @param rootPath The Root XPath
     */
    public void setRootPath(XPathHolder rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * A set of mandaroty XPaths
     * @return The set of mandatory XPaths
     */
    @XmlElement
    public Set<String> getMandatoryXpath() {
        return mandatoryXpath;
    }

    /**
     * Set the mandatory xpaths
     * @param mandatoryXpath
     */
    public void setMandatoryXpath(Set<String> mandatoryXpath) {
        this.mandatoryXpath = mandatoryXpath;
    }

    /**
     * The string of the schematron rules for the schema. These rules are already
     * transformed to XSL and are applied post transformation to perform the validation
     * @return The string of the schematron rules for the schema.
     */
    @XmlElement
    public Set<String> getSchematronRules() {
        return schematronRules;
    }

    /**
     * Set the schematron rules for a mapping
     * @param schematronRules The schematron rules for a mapping
     */
    public void setSchematronRules(Set<String> schematronRules) {
        this.schematronRules = schematronRules;
    }
    @XmlElement
    public Map<String, String> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Map<String, String> documentation) {
        this.documentation = documentation;
    }
}
