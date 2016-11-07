package eu.europeana.metis.linkchecking;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 11/4/16.
 */
@XmlRootElement
public enum EdmFieldName {
    @XmlEnumValue("edm:isShownBy")
    EDM_ISHOWNBY("edm:isShownBy"),
    @XmlEnumValue("edm:isShownAt")
    EDM_ISSHOWNAT("edm:isShownAt"),
    @XmlEnumValue("edm:object")
    EDM_OBJECT("edm:object"),
    @XmlEnumValue("edm:hasView")
    EDM_HASVIEW("edm:hasView");

    private String name;
    EdmFieldName(String name){
        this.name=name;
    }

    public String getFieldName(){
        return this.name;
    }
}
