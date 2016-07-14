package eu.europeana.itemization;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * An itemization result
 * Created by ymamakis on 2/9/16.
 */
@XmlRootElement
public class RequestResult {

    /**
     * A list of itemized EDM records
     */

    private List<String> itemizedRecords;
    @XmlElement
    public List<String> getItemizedRecords() {
        return itemizedRecords;
    }

    public void setItemizedRecords(List<String> itemizedRecords) {
        this.itemizedRecords = itemizedRecords;
    }


}
