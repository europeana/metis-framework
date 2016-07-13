package eu.europeana.metis.framework.rest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Generic Exception Handler
 * Created by ymamakis on 2/25/16.
 */
@Controller
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServerError handleException(HttpServletRequest req, Exception exception){
        return new ServerError(exception.getMessage(),req.getRequestURI());
    }

}


