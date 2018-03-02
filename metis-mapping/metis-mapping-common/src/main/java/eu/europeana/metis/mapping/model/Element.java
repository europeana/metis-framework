package eu.europeana.metis.mapping.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

/**
 * An Element definition according to the XSD.
 * This class extends the Attribute by allowing hierarchies of Elements and Attributes
 *
 * @see Attribute
 * <p>
 * Created by ymamakis on 4/7/16.
 */
@Entity
@XmlRootElement
public class Element extends Attribute {


    @Reference
    private List<Element> elements;
    @Reference
    private List<Attribute> attributes;
    private Set<String> mandatoryXpath;
    private String type;
    private boolean hasMapping;
    private String documentation;


    /**
     * The list of child Elements to this Element
     *
     * @return The list of child Elements to this Element
     */
    @XmlElement(name = "children")
    public List<Element> getElements() {
        return elements;
    }

    /**
     * Set the list of Elements for this Element
     *
     * @param elements The list of child Elements
     */
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    /**
     * Set the list of Attributes of this Element
     *
     * @param attributes The list of Attributes for this Element
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Get the list of Attributes for this Element
     *
     * @return The lit of Attributes for this Element
     */
    @XmlElement
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * The type of field (e.g. string)
     *
     * @return The type of field
     */
    @XmlElement
    public String getType() {
        return type;
    }

    /**
     * Set the type of field
     *
     * @param type The type of field
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Flag indicating whether the Element or its Child elements and attributes have a mapping
     *
     * @return Flag indicating whether the Element or its Child elements and attributes have a mapping
     */
    @XmlElement
    public boolean isHasMapping() {
        return hasMapping;
    }

    /**
     * Set the flag indicating whether the Element or its Child elements and attributes have a mapping
     *
     * @param hasMapping
     */
    public void setHasMapping(boolean hasMapping) {
        this.hasMapping = hasMapping;
    }

    /**
     * Return the set of XPaths of the children Elements or Attributes that are mandatory,
     * starting from the current Element
     *
     * @return the set of XPaths of the children Elements or Attributes that are mandatory,
     * starting from the current Element
     */
    @XmlElement
    public Set<String> getMandatoryXpath() {
        return mandatoryXpath;
    }

    /**
     * the set of XPaths of the children Elements or Attributes that are mandatory,
     * starting from the current Element
     *
     * @param mandatoryXpath the set of XPaths of the children Elements or Attributes that are mandatory,
     *                       starting from the current Element
     */
    public void setMandatoryXpath(Set<String> mandatoryXpath) {
        this.mandatoryXpath = mandatoryXpath;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
