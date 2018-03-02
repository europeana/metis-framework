package eu.europeana.hierarchy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Wrapper for batch operation responses
 * Created by ymamakis on 1/22/16.
 */
@XmlRootElement
public class ParentNodeList {
    @XmlElement
    private Set<ParentNode> parentNodeList;

    public Set<ParentNode> getParentNodeList() {
        return parentNodeList;
    }

    public void setParentNodeList(Set<ParentNode> parentNodeList) {
        this.parentNodeList = parentNodeList;
    }
}
