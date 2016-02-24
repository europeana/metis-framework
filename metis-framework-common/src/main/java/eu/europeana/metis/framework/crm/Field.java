package eu.europeana.metis.framework.crm;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by ymamakis on 2/23/16.
 */
public class Field {

    private String val;

    private String content;
    @XmlElement
    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
    @XmlElement
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
