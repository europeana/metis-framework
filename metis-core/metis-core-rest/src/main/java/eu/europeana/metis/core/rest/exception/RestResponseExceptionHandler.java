package eu.europeana.metis.core.rest.exception;

import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserNotFoundException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.StructuredExceptionWrapper;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * {@link ControllerAdvice} class that handles exceptions through spring.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@ControllerAdvice
public class RestResponseExceptionHandler {

  /**
   * Handle metis exceptions using their corresponding custom created exception.
   *
   * @param exception the exception thrown
   * @param response the response that should be updated
   * @return {@link StructuredExceptionWrapper} a json friendly class that contains the error message for the client
   */
  @ExceptionHandler(value = {UserNotFoundException.class, IOException.class, ServletException.class,
      BadContentException.class, DatasetAlreadyExistsException.class,
      NoDatasetFoundException.class, NoWorkflowFoundException.class,
      NoScheduledWorkflowFoundException.class, WorkflowAlreadyExistsException.class,
      WorkflowExecutionAlreadyExistsException.class,
      ScheduledWorkflowAlreadyExistsException.class,
      NoWorkflowExecutionFoundException.class, ExecutionException.class,
      InterruptedException.class, UserUnauthorizedException.class, PluginExecutionNotAllowed.class,
      ExternalTaskException.class})
  @ResponseBody
  public StructuredExceptionWrapper handleException(Exception exception,
      HttpServletResponse response) {
    HttpStatus status = AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class).value();
    response.setStatus(status.value());
    return new StructuredExceptionWrapper(exception.getMessage());
  }

  /**
   * Handler for specific classes to overwrite behaviour
   * @param exception the exception thrown
   * @param response the response that should be updated
   * @return {@link StructuredExceptionWrapper} a json friendly class that contains the error message for the client
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMessageNotReadable(HttpMessageNotReadableException exception,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(
        "Message body not readable. It is missing or malformed\n" + exception.getMessage());
  }

  /**
   * Handler for specific classes to overwrite behaviour
   * @param exception the exception thrown
   * @param response the response that should be updated
   * @return {@link StructuredExceptionWrapper} a json friendly class that contains the error message for the client
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMissingParams(MissingServletRequestParameterException exception,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(exception.getParameterName() + " parameter is missing");
  }

  /**
   * Handler for specific classes to overwrite behaviour
   * @param exception the exception thrown
   * @param response the response that should be updated
   * @return {@link StructuredExceptionWrapper} a json friendly class that contains the error message for the client
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMissingParams(HttpRequestMethodNotSupportedException exception,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
    return new StructuredExceptionWrapper("Method not allowed: " + exception.getMessage());
  }

  /**
   * Handler for specific classes to overwrite behaviour
   * @param exception the exception thrown
   * @param response the response that should be updated
   * @return {@link StructuredExceptionWrapper} a json friendly class that contains the error message for the client
   */
  @ExceptionHandler(value = {IllegalStateException.class,
      MethodArgumentTypeMismatchException.class})
  @ResponseBody
  public StructuredExceptionWrapper handleMessageNotReadable(Exception exception,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(
        "Request not readable.\n" + exception.getMessage());
  }
}
