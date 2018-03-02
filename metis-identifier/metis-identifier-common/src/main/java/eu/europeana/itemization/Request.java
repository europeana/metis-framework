package eu.europeana.itemization;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * An Itemization request for list of records
 * Created by ymamakis on 2/9/16.
 */
@XmlRootElement
public class Request {

    /**
     * A list of records as string
     */

    private List<String> records;
    @XmlElement
    public List<String> getRecords() {
        return records;
    }

    public void setRecords(List<String> records) {
        this.records = records;
    }
}
