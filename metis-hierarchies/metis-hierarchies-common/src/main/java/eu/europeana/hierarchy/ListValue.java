package eu.europeana.hierarchy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Class that is used to represent EDM properties that have a list of values
 * Created by ymamakis on 1/21/16.
 */
@XmlRootElement
public class ListValue {
    @XmlElement
    private String key;

    @XmlElement
    private List<String> value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}
