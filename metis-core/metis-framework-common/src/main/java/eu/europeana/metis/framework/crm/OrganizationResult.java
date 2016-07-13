package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * A result wrapper for Zoho
 * Created by ymamakis on 2/23/16.
 */

public class OrganizationResult {

    /**
     * The module name Zoho communicates to (it is part of the URL as well)
     */
    @JsonProperty(value="CustomModule1")
    private Module module;



    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

}
