package eu.europeana.metis.mapping.xsd;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * File Upload DTO
 * Created by ymamakis on 8/3/16.
 */
@XmlRootElement
public class FileXSDUploadDTO extends AbstractXSDUploadDTO {
    private byte[] file;

    @XmlElement
    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
