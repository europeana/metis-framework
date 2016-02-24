package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Zoho client response
 * Created by ymamakis on 2/23/16.
 */

public class ZohoResponse {

    /**
     * The response wrapper
     */
    @JsonProperty(value="response")
    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
