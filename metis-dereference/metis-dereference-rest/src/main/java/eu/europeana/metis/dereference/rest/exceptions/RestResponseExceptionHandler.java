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
