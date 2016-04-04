package eu.europeana.metis.framework.crm;

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
