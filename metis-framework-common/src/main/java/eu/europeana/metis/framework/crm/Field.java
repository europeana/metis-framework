package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;

/**
 * A field representation in Zoho. Each field has a val (the name of the field) and content (the value of the field)
 * Created by ymamakis on 2/23/16.
 */
public class Field {

    /**
     * The name of the field
     */
    @JsonProperty(value = "val")
    private String val;
    /**
     * The value of the field
     */
    @JsonProperty(value = "content")
    private String content;


    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
