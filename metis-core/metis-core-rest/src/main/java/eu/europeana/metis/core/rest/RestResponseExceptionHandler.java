package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.exceptions.StructuredExceptionWrapper;
import eu.europeana.metis.core.exceptions.UserNotFoundException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@ControllerAdvice
public class RestResponseExceptionHandler {

  @ExceptionHandler(value = {UserNotFoundException.class })
  @ResponseBody
  public StructuredExceptionWrapper handleUserNotFound(HttpServletRequest request, Exception ex) {
    return new StructuredExceptionWrapper(404 , ex.getMessage());
  }
}
