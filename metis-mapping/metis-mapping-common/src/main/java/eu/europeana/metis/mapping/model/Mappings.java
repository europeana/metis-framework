package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Field mappings class. It also contains information about the namespaces, their prefixes and
 * the rootElement (wrapper element) of the documents to be mapped
 * The wrapper element XPath should not be null
 * TODO: allow the wrapper element XPath to be null
 * Created by gmamakis on 8-4-16.
 */
@XmlRootElement
@Entity
public class Mappings {
    @Id
    private ObjectId id;
    @Reference
    private List<Element> elements;
    @Reference
    private List<Attribute> attributes;
    @Transient
    private Map<String,String> namespaces = new HashMap<>();
    private String rootElement;
    private boolean hasMappings;
    @Embedded
    private List<Namespace> ns = new ArrayList<>();
    /**
     * The Elements that are direct children of the root Element
     * @return The Elements that are direct children of the root Element
     */
    @XmlElement
    public List<Element> getElements() {
        return elements;
    }

    /**
     * The elements that are direct children of the root Element
     * @param elements The elements that are direct children of the root Element
     */
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    /**
     * The attributes of the root Element
     * @return The attributes of the root Element
     */
    @XmlElement
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * The attributes of the root Element
     * @param attributes The attributes of the root Element
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Get the namespaces and their prefixes
     * @return The namespaces defined in the mapping and their prefixes
     * (these are filled in during the reading of the XSD that generates the mapping)
     */
    @XmlElement

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * The namespaces and their prefixes as a key-value map
     * @param namespaces The namespaces and their prefixes
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Flag indicating whether the mappings is actually filled in. This flag is set to true when the first Element or
     * Attribute gets a {@link IMapping}
     * @return A flag indicating whether the mappings are populated or not
     */
    @XmlElement
    public boolean isHasMappings() {
        return hasMappings;
    }

    /**
     * Set the fag to indicate that the mapping is populated
     * @param hasMappings The flag that indicates that the mappings are populated
     */
    public void setHasMappings(boolean hasMappings) {
        this.hasMappings = hasMappings;
    }

    /**
     * The XPath of the wrapper element
     * @return The XPath of the wrapper element
     */
    @XmlElement
    public String getRootElement() {
        return rootElement;
    }

    /**
     * Set the XPath of the root element
     * @param rootElement The XPath of the root Element
     */
    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

    /**
     * The id of the Mappings
     * @return The id of the Mappings
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id of the mappings
     * @param id the id of the mappings
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    @PostLoad
    public void populateNameSpaces(){
        for(Namespace nsOne:ns){
            namespaces.put(nsOne.getUri(),nsOne.getPrefix());
        }
    }

    @PrePersist
    public void populateNS(){
        for(Map.Entry<String,String> nss :namespaces.entrySet()){
            Namespace nsOne  = new Namespace();
            nsOne.setPrefix(nss.getValue());
            nsOne.setUri(nss.getKey());
            ns.add(nsOne);
        }
    }

}
