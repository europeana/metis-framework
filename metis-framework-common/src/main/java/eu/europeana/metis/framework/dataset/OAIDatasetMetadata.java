package eu.europeana.metis.framework.dataset;

import eu.europeana.metis.framework.common.OAIMetadata;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * OAI dataset specific metadata
 * Created by ymamakis on 2/17/16.
 */

@Entity
@XmlRootElement
public class OAIDatasetMetadata extends OAIMetadata {

    /**
     * The setSpec
     */
    private String setSpec;
    @XmlElement
    public String getSetSpec() {
        return setSpec;
    }

    public void setSetSpec(String setSpec) {
        this.setSpec = setSpec;
    }
}
