package eu.europeana.hierarchy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Input Node for neo4j indexing supporting String and List
 * Created by ymamakis on 1/21/16.
 */
@XmlRootElement
public class InputNode {

    @XmlElement
    private Set<StringValue> stringValues;

    @XmlElement
    private Set<ListValue> listValues;

    public Set<StringValue> getStringValues() {
        return stringValues;
    }

    public void setStringValues(Set<StringValue> stringValues) {
        this.stringValues = stringValues;
    }

    public Set<ListValue> getListValues() {
        return listValues;
    }

    public void setListValues(Set<ListValue> listValues) {
        this.listValues = listValues;
    }
}
