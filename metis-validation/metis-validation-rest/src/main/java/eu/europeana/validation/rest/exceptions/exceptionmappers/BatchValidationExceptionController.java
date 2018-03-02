package eu.europeana.validation.rest.exceptions.exceptionmappers;

import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.rest.exceptions.BatchValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 2/24/16.
 */

@ControllerAdvice
public class BatchValidationExceptionController {

    /**
     * Handles specified exception
     *
     * @param e exception to be handled
     * @return
     */
    @ExceptionHandler(BatchValidationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ValidationResultList handleException(BatchValidationException e) {
        return e.getList();
    }
}
