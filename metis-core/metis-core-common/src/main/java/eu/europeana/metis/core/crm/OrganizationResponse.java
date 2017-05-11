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

package eu.europeana.metis.core.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A OrganizationResponse wrapper for Zoho
 * Created by ymamakis on 2/24/16.
 */

public class OrganizationResponse {

    /**
     * The organizationResult of Zoho
     */
    @JsonProperty(value=  "result")
    private OrganizationResult organizationResult;

    /**
     * THe URI of the call
     */
    @JsonProperty(value=  "uri")
    private String uri;

    public OrganizationResult getOrganizationResult() {
        return organizationResult;
    }

    public void setOrganizationResult(OrganizationResult organizationResult) {
        this.organizationResult = organizationResult;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
