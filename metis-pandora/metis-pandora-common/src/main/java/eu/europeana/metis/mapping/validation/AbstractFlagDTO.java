package eu.europeana.metis.mapping.validation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Flag marking DTO
 * Created by ymamakis on 8/3/16.
 */
@XmlRootElement
public abstract class AbstractFlagDTO {
    private String message;

    @XmlElement
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
