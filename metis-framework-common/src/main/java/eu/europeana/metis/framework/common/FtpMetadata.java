package eu.europeana.metis.framework.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class FtpMetadata extends HttpMetadata {
    @XmlElement
    private String ftpServerAddress;

    public String getFtpServerAddress() {
        return ftpServerAddress;
    }

    public void setFtpServerAddress(String ftpServerAddress) {
        this.ftpServerAddress = ftpServerAddress;
    }
}
