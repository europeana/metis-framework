package eu.europeana.validation.rest.exceptions.exceptionmappers;

import eu.europeana.validation.rest.exceptions.ServerException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 2/24/16.
 */
@ControllerAdvice
public class ServerExceptionMapper {

    /**
     * Handles specified exception
     *
     * @param e exception to be handled
     * @return
     */
    @ExceptionHandler(ServerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(ServerException e) {
        return "Server exception: [" + e.getMessage() + "]";
    }
}
