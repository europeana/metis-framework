package eu.europeana.metis.mapping.validation;

import eu.europeana.metis.mapping.model.Element;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Element Flag marking DTO
 * Created by ymamakis on 8/3/16.
 */
@XmlRootElement
public class ElementFlagDTO extends AbstractFlagDTO{
    private Element elem;

    @XmlElement
    public Element getElem() {
        return elem;
    }

    public void setElem(Element elem) {
        this.elem = elem;
    }
}
