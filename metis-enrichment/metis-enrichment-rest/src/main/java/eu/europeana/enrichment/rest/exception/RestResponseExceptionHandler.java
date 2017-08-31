package eu.europeana.enrichment.rest.exception;

import eu.europeana.metis.exception.StructuredExceptionWrapper;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by ymamakis on 9/29/16.
 */
@ControllerAdvice
public class RestResponseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(EnrichmentException.class)
    public StructuredExceptionWrapper handleResponse(HttpServletResponse response,
        EnrichmentException e) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return new StructuredExceptionWrapper(e.getMessage());
    }
}