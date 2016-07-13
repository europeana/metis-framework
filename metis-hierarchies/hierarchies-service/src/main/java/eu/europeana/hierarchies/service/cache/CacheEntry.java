package eu.europeana.hierarchies.service.cache;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Object model of a cache entry
 * Created by ymamakis on 1/25/16.
 */
@XmlRootElement
public class CacheEntry{

    /**
     * The collection entry in cache
     */
    @XmlElement
    private String collection;
    /**
     * The string of parents
     */
    @XmlElement
    private Set<String> parents;

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Set<String> getParents() {
        return parents;
    }

    public void setParents(Set<String> parents) {
        this.parents = parents;
    }


}
