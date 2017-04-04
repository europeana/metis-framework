package eu.europeana.metis.mapping.validation;

import eu.europeana.metis.mapping.model.Attribute;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Attribute flag marking DTO
 * Created by ymamakis on 8/3/16.
 */
@XmlRootElement
public class AttributeFlagDTO extends AbstractFlagDTO {
    private Attribute attr;

    @XmlElement
    public Attribute getAttr() {
        return attr;
    }

    public void setAttr(Attribute attr) {
        this.attr = attr;
    }
}
