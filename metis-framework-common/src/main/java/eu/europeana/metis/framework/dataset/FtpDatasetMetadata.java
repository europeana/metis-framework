package eu.europeana.metis.framework.dataset;

import eu.europeana.metis.framework.common.FtpMetadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class FtpDatasetMetadata extends FtpMetadata{

    @XmlElement
    private String ftpUrl;

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }
}
