package eu.europeana.metis.dereference;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

/**
 * A controlled vocabulary representation
 * Created by ymamakis on 2/11/16.
 */

@Entity("Vocabulary")
public class Vocabulary implements Serializable {

	/** Required for implementations of {@link Serializable}. **/
	private static final long serialVersionUID = 2946293185967000824L;
	
	@Id
    private String id;
	
    /**
     * The URI of the controlled vocabulary
     */
    @Indexed
    private String uri;

    /**
     * The suffix of the vocabulary: needs to be added after the variable bit of the URI.
     */
    private String suffix;
    
    /**
     * Rules that take into account the rdf:type attribute of an rdf:Description to specify whether
     */
    private Set<String> typeRules;

    /**
     * Rules by URL
     */
    private Set<String> rules;

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
    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    private ContextualClass type;

    @XmlElement
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    @XmlElement
    public String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
  
    @XmlElement
    public Set<String> getTypeRules() {
        return this.typeRules == null ? null : Collections.unmodifiableSet(this.typeRules);
    }
  
    public void setTypeRules(Set<String> typeRules) {
        if (typeRules == null || typeRules.isEmpty()) {
            this.typeRules = null;
        } else {
            this.typeRules = new HashSet<>(typeRules);
        }
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
    public Set<String> getRules() {
      return this.rules == null ? null : Collections.unmodifiableSet(this.rules);
    }

    public void setRules(Set<String> rules) {
        if (rules == null || rules.isEmpty()) {
            this.rules = null;
        } else {
            this.rules = new HashSet<>(rules);
            // For people who have old stubborn habits.
            this.rules.remove("*"); 
        }
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
