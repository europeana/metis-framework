package eu.europeana.metis.framework.crm;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A Response wrapper for Zoho
 * Created by ymamakis on 2/24/16.
 */

public class Response {

    /**
     * The result of Zoho
     */
    @JsonProperty(value=  "result")
    private Result result;

    /**
     * THe URI of the call
     */
    @JsonProperty(value=  "uri")
    private String uri;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
