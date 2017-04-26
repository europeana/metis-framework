package eu.europeana.metis.framework.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ymamakis on 2/26/16.
 */
public class ServerError{

    @JsonProperty(value = "errorMessage")
    private String message;
    @JsonProperty(value = "requestURI")
    private String uri;
    public  ServerError(String message, String uri){
        this.message = message;
        this.uri = uri;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
