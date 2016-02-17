package eu.europeana.metis.framework.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class FolderMetadata extends HarvestingMetadata{

    @XmlElement
    private String recordXPath;

    public String getRecordXPath() {
        return recordXPath;
    }

    public void setRecordXPath(String recordXPath) {
        this.recordXPath = recordXPath;
    }
}
