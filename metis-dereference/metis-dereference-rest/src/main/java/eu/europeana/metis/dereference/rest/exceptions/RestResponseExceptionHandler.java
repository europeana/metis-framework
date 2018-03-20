package eu.europeana.metis.dereference.rest.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Generic Exception Handler
 * Created by ymamakis on 2/25/16.
 **/
@ControllerAdvice
public class RestResponseExceptionHandler {
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ServerError handleResponse(HttpServletResponse response,HttpServletRequest req,
        Exception exception) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ServerError(exception.getMessage());
    }
}

class ServerError{

    @JsonProperty(value = "errorMessage")
    private String message;

    public  ServerError(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
