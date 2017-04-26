package eu.europeana.metis.linkchecking;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by ymamakis on 11/4/16.
 */
@XmlRootElement
public class LinkcheckRequest {

    private EdmFieldName fieldName;

    private List<String> urls;
    @XmlElement
    public EdmFieldName getFieldName() {
        return fieldName;
    }

    public void setFieldName(EdmFieldName fieldName) {
        this.fieldName = fieldName;
    }
    @XmlElement
    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
