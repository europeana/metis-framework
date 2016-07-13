package eu.europeana.metis.framework.common;


import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Ftp specific technical metadata
 * Created by ymamakis on 2/17/16.
 */
@Entity
@XmlRootElement
public class FtpMetadata extends HttpMetadata {

    /**
     * Ftp Server address
     */
    private String ftpServerAddress;
    @XmlElement
    public String getFtpServerAddress() {
        return ftpServerAddress;
    }

    public void setFtpServerAddress(String ftpServerAddress) {
        this.ftpServerAddress = ftpServerAddress;
    }
}
