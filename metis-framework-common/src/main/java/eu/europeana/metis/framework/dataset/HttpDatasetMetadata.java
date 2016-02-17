package eu.europeana.metis.framework.dataset;

import eu.europeana.metis.framework.common.HttpMetadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class HttpDatasetMetadata extends HttpMetadata {

    @XmlElement
    private String httpUrl;

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }
}
