package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Zoho client organizationResponse
 * Created by ymamakis on 2/23/16.
 */

public class ZohoContactResponse {

    /**
     * The organizationResponse wrapper
     */
    @JsonProperty(value="response")
    private ContactResponse contactResponse;

    public ContactResponse getContactResponse() {
        return contactResponse;
    }

    public void setContactResponse(ContactResponse contactResponse) {
        this.contactResponse = contactResponse;
    }
}
