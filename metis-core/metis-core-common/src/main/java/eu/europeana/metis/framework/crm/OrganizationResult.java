/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

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
