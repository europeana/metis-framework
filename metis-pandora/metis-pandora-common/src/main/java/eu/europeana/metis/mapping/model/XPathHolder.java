package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * XPath place holder
 * TODO: can this be merged or removed whatsoever?
 * Created by ymamakis on 4/7/16.
 */
@Entity
@XmlRootElement
public class XPathHolder {

    @Id
    private ObjectId id;

    private String xpath;
    private String uri;
    private String uriPrefix;
    @Indexed (unique = false)
    private String name;
    private boolean optional;
    private boolean multiple;
    private String description;
    @Embedded
    private List<XPathHolder> children;

    /**
     * Get the XPath of the current XpathHolder
     * @return The XPath of the current XPath holder
     */
    @XmlElement
    public String getXpath() {
        return xpath;
    }

    /**
     * Set the XPath of the current XPathholder
     * @param xpath XPath to set
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * Get the uri of the current XPath holder
     * @return The uri of the XPath holder
     */
    @XmlElement
    public String getUri() {
        return uri;
    }

    /**
     * Set the URI of the XPathHolder
     * @param uri The URI of the XPath holder
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * The URI prefix of the current XPath Holder
     * @return The URI of the current XPath holder
     */
    @XmlElement
    public String getUriPrefix() {
        return uriPrefix;
    }

    /**
     * Set the URI prefix of the current XPath holder
     * @param uriPrefix
     */
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    /**
     * The name of the current XPath holder
     * @return The name of the current XPath holder
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * The name of the current XPath holder
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Check if the XPath holder is optional
     * @return Whether the XPath Holder is optional or mandatory
     */
    @XmlElement
    public boolean isOptional() {
        return optional;
    }

    /**
     * Set the XPathholder as optional (cardinality=0)
     * @param optional whether the XPAthHolder is optional
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * Check whether the XPAthHolder has a cardinality > 1
     * @return whether the XPathHolder has a cardinality > 1
     */
    @XmlElement
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * Set the cardinality of the XPathholder as unbounded
     * @param multiple whether the XPAthHolder is unbounded
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    /**
     * Get the description of the XPathHolder
     * @return the description of the XPathHolder
     */
    @XmlElement
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the XPathHolder
     * @param description The description of the XPathHolder
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The XPathHolder of the description of the children
     * @return The List of XPaths of the children
     */
    @XmlElement
    public List<XPathHolder> getChildren() {
        return children;
    }

    /**
     * The XPathHolder of the description of the children
     * @param children The List of XPaths of the children
     */
    public void setChildren(List<XPathHolder> children) {
        this.children = children;
    }

    /**
     * The id of the XPath holder
     * @return the ID of the XPathHolder
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the XPathHolder
     * @param id the Id of the XPathHolder
     */
    public void setId(ObjectId id) {
        this.id = id;
    }


}

