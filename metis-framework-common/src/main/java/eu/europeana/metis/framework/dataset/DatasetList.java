package eu.europeana.metis.framework.dataset;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by ymamakis on 3/2/16.
 */
@XmlRootElement
public class DatasetList {

    private List<Dataset> datasetList;

    public List<Dataset> getDatasetList() {
        return datasetList;
    }

    @XmlElement
    public void setDatasetList(List<Dataset> datasetList) {
        this.datasetList = datasetList;
    }
}
