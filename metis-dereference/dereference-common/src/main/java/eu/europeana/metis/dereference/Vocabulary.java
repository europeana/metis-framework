package eu.europeana.metis.dereference;


import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * A controlled vocabulary representation
 * Created by ymamakis on 2/11/16.
 */

@Entity("Vocabulary")
public class Vocabulary implements Serializable{


    @Id
    private String id;
    /**
     * The URI of the controlled vocabulary
     */
    @Indexed(unique = false)
    private String URI;

    /**
     * Rules that take into account the rdf:type attribute of an rdf:Description to specify whether
     */
    private String typeRules;

    /**
     * Rules by URL
     */
    private String rules;

    /**
     * The XSLT to convert an external entity to an internal entity
     */
    private String xslt;

    /**
     * The iterations (broader) that we need to retrieve
     */
    private int iterations;

    /**
     * The name of the vocabulary
     */
    @Indexed(unique = true)
    private String name;

    private ContextualClass type;

    @XmlElement
    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    @XmlElement
    public String getTypeRules() {
        return typeRules;
    }

    public void setTypeRules(String typeRules) {
        this.typeRules = typeRules;
    }

    @XmlElement
    public String getXslt() {
        return xslt;
    }

    public void setXslt(String xslt) {
        this.xslt = xslt;
    }

    @XmlElement
    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    @XmlElement
    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public ContextualClass getType() {
        return type;
    }

    public void setType(ContextualClass type) {
        this.type = type;
    }
    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
