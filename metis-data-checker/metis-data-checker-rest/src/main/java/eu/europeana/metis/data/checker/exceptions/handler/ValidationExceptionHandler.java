package eu.europeana.metis.data.checker.exceptions.handler;

import eu.europeana.metis.data.checker.common.exception.DataCheckerServiceException;
import eu.europeana.metis.data.checker.common.exception.DataCheckerValidationException;
import eu.europeana.metis.data.checker.common.exception.ZipFileException;
import eu.europeana.metis.exception.StructuredExceptionWrapper;
import eu.europeana.validation.model.ValidationResultList;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 9/29/16.
 */
@ControllerAdvice
public class ValidationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(DataCheckerValidationException.class)
    public ValidationResultList handleResponse(DataCheckerValidationException e){
        return e.getValidationResult();
    }

    @ExceptionHandler(ZipFileException.class)
    @ResponseBody
    public StructuredExceptionWrapper handleMessageNotReadable(ZipFileException ex,
        HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return new StructuredExceptionWrapper(ex.getMessage());
    }

    @ExceptionHandler(DataCheckerServiceException.class)
    @ResponseBody
    public StructuredExceptionWrapper handleMessageNotReadable(DataCheckerServiceException ex,
        HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new StructuredExceptionWrapper(ex.getMessage());
    }
}
