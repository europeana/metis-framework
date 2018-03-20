package eu.europeana.metis.mapping.model;

import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Namespace object model
 * Created by ymamakis on 6/27/16.
 */
@XmlRootElement
@Entity
public class Namespace {
    private String prefix;
    private String uri;

    @XmlElement
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @XmlElement
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
