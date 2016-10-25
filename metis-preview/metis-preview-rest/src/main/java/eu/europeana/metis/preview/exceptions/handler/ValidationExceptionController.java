package eu.europeana.metis.preview.exceptions.handler;

import eu.europeana.metis.preview.exceptions.PreviewValidationException;
import eu.europeana.validation.model.ValidationResultList;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 9/29/16.
 */
@ControllerAdvice
public class ValidationExceptionController {
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(PreviewValidationException.class)
    public ValidationResultList handleResponse(PreviewValidationException e){
        return e.getValidationResult();
    }
}
