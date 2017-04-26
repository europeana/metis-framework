package eu.europeana.metis.linkchecking;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 11/4/16.
 */
@XmlRootElement
public class LinkcheckStatus {

    private EdmFieldName edmFieldName;
    private int failed;
    private int succeeded;

    @XmlElement
    public EdmFieldName getEdmFieldName() {
        return edmFieldName;
    }

    public void setEdmFieldName(EdmFieldName edmFieldName) {
        this.edmFieldName = edmFieldName;
    }

    @XmlElement
    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    @XmlElement
    public int getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }
}
