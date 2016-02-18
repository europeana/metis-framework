package eu.europeana.metis.framework.common;


import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Folder specific metadata
 * Created by ymamakis on 2/17/16.
 */
@Entity
@XmlRootElement
public class FolderMetadata extends HarvestingMetadata{


    /**
     * Where to find records in the folder
     */
    private String recordXPath;

    @XmlElement
    public String getRecordXPath() {
        return recordXPath;
    }

    public void setRecordXPath(String recordXPath) {
        this.recordXPath = recordXPath;
    }
}
