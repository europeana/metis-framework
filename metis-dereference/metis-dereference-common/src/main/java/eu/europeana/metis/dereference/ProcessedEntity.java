package eu.europeana.metis.dereference;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A processed (mapped) Entity
 * Created by ymamakis on 2/11/16.
 */
@XmlRootElement
public class ProcessedEntity {

    /**
     * The URI of the Entity
     */
    private String URI;

    /**
     * A xml representation of the mapped Entity in one of the contextual resources
     */
    private String xml;
    @XmlElement
    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }
    @XmlElement
    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

}
