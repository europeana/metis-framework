package eu.europeana.redirects.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A redirect response
 * Created by ymamakis on 1/13/16.
 */
@XmlRootElement
public class RedirectResponse {
    private String newId;
    private String oldId;
    @XmlElement
    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }
    @XmlElement
    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }
}
