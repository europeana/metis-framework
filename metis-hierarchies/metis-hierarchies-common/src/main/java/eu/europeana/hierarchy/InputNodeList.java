package eu.europeana.hierarchy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Wrapper for batch input nodes
 * Created by ymamakis on 1/22/16.
 */
@XmlRootElement
public class InputNodeList {

    @XmlElement
    private List<InputNode> inputNodeList;

    public List<InputNode> getInputNodeList() {
        return inputNodeList;
    }

    public void setInputNodeList(List<InputNode> inputNodeList) {
        this.inputNodeList = inputNodeList;
    }
}
