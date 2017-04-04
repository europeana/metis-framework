package eu.europeana.metis.mapping.xsd;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * URL Upload DTO
 * Created by ymamakis on 8/3/16.
 */
@XmlRootElement
public class UrlXSDUploadDTO extends AbstractXSDUploadDTO {
    private String url;

    @XmlElement
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
