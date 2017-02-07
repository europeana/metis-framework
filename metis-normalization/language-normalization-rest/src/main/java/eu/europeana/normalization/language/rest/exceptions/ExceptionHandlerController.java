package eu.europeana.normalization.language.rest.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Generic Exception Handler Created by ymamakis on 2/25/16.
 */
@Controller
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServerError handleException(HttpServletRequest req, Exception exception) {
        return new ServerError(exception.getMessage(), req.getRequestURI());
    }

}

class ServerError {

    @JsonProperty(value = "errorMessage")
    private String message;
    @JsonProperty(value = "requestURI")
    private String uri;

    public ServerError(String message, String uri) {
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
