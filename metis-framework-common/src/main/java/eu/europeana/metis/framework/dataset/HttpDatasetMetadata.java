package eu.europeana.metis.framework.dataset;

import eu.europeana.metis.framework.common.HttpMetadata;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HTTP dataset specific metadata
 * Created by ymamakis on 2/17/16.
 */
@Entity
@XmlRootElement
public class HttpDatasetMetadata extends HttpMetadata {

    /**
     * The URL of the dataset
     */
    private String httpUrl;
    @XmlElement
    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }
}
