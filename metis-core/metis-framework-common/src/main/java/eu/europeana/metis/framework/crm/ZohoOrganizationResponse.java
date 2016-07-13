package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Zoho client organizationResponse
 * Created by ymamakis on 2/23/16.
 */

public class ZohoOrganizationResponse {

    /**
     * The organizationResponse wrapper
     */
    @JsonProperty(value="response")
    private OrganizationResponse organizationResponse;

    public OrganizationResponse getOrganizationResponse() {
        return organizationResponse;
    }

    public void setOrganizationResponse(OrganizationResponse organizationResponse) {
        this.organizationResponse = organizationResponse;
    }
}
