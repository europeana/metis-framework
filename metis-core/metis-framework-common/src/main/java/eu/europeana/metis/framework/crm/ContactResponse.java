package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A OrganizationResponse wrapper for Zoho
 * Created by ymamakis on 2/24/16.
 */

public class ContactResponse {

    /**
     * The organizationResult of Zoho
     */
    @JsonProperty(value=  "result")
    private ContactResult contactResult;

    /**
     * THe URI of the call
     */
    @JsonProperty(value=  "uri")
    private String uri;

    public ContactResult getContactResult() {
        return contactResult;
    }

    public void setContactResult(ContactResult contactResult) {
        this.contactResult = contactResult;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
