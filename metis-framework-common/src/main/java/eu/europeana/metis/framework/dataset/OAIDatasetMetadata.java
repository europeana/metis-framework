package eu.europeana.metis.framework.dataset;

import eu.europeana.metis.framework.common.OAIMetadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class OAIDatasetMetadata extends OAIMetadata {

    @XmlElement
    private String setSpec;

    public String getSetSpec() {
        return setSpec;
    }

    public void setSetSpec(String setSpec) {
        this.setSpec = setSpec;
    }
}
