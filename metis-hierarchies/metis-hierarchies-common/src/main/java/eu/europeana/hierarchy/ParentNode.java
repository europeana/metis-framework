package eu.europeana.hierarchy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to denote the parent node of a generated node
 * Created by ymamakis on 1/22/16.
 */
@XmlRootElement
public class ParentNode {
    @XmlElement
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
