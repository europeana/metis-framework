package eu.europeana.validation.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 9/12/16.
 */
@XmlRootElement
public class Record {

    private String record;

    @XmlElement
    public String getRecord(){
        return record;
    }

    public void setRecord(String record){
        this.record = record;
    }
}
