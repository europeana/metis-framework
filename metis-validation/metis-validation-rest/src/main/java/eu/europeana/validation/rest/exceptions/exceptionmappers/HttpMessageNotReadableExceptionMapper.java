package eu.europeana.validation.rest.exceptions.exceptionmappers;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by pwozniak on 12/7/17
 */
@ControllerAdvice
public class HttpMessageNotReadableExceptionMapper {

    /**
     * Handles specified exception
     *
     * @param ex exception to be handled
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return "Request body is not readable. [" + ex.getMessage() + "]";
    }
}
