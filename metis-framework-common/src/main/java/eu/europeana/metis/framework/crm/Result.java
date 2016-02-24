package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 2/23/16.
 */

public class Result {
    @JsonProperty(value="CustomModule1")
    private Module module;



    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

}
