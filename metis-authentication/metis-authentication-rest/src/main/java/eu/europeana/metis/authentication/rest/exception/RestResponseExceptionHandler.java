package eu.europeana.metis.authentication.rest.exception;

import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.StructuredExceptionWrapper;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.TransactionException;
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
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@ControllerAdvice
public class RestResponseExceptionHandler {

  /**
   * Handle user controlled exceptions.
   *
   * @param ex the exception produced
   * @param response the response that needs to have it's status modified
   * @return {@link StructuredExceptionWrapper} that has the exception message wrapped
   */
  @ExceptionHandler(GenericMetisException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleException(Exception ex, HttpServletResponse response) {
    HttpStatus status = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class).value();
    response.setStatus(status.value());
    return new StructuredExceptionWrapper(ex.getMessage());
  }

  /**
   * Handle a {@link HttpMessageNotReadableException}
   *
   * @param ex the exception produced
   * @param response the response that needs to have it's status modified
   * @return {@link StructuredExceptionWrapper} that has the exception message wrapped
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMessageNotReadable(HttpMessageNotReadableException ex,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(
        "Message body not readable. It is missing or malformed\n" + ex.getMessage());
  }

  /**
   * Handle a {@link MissingServletRequestParameterException}
   *
   * @param ex the exception produced
   * @param response the response that needs to have it's status modified
   * @return {@link StructuredExceptionWrapper} that has the exception message wrapped
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMissingParams(MissingServletRequestParameterException ex,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(ex.getParameterName() + " parameter is missing");
  }

  /**
   * Handle a {@link HttpRequestMethodNotSupportedException}
   *
   * @param ex the exception produced
   * @param response the response that needs to have it's status modified
   * @return {@link StructuredExceptionWrapper} that has the exception message wrapped
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMissingParams(HttpRequestMethodNotSupportedException ex,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
    return new StructuredExceptionWrapper("Method not allowed: " + ex.getMessage());
  }

  /**
   * Handle a {@link IllegalStateException} and {@link MethodArgumentTypeMismatchException}
   *
   * @param ex the exception produced
   * @param response the response that needs to have it's status modified
   * @return {@link StructuredExceptionWrapper} that has the exception message wrapped
   */
  @ExceptionHandler(value = {IllegalStateException.class,
      MethodArgumentTypeMismatchException.class})
  @ResponseBody
  public StructuredExceptionWrapper handleMessageNotReadable(Exception ex,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(
        "Request not readable.\n" + ex.getMessage());
  }

  /**
   * Handle a {@link TransactionException}
   *
   * @param ex the exception produced
   * @param response the response that needs to have it's status modified
   * @return {@link StructuredExceptionWrapper} that has the exception message wrapped
   */
  @ExceptionHandler(value = {TransactionException.class})
  @ResponseBody
  public StructuredExceptionWrapper handleMessageTransactionException(Exception ex,
      HttpServletResponse response) {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    return new StructuredExceptionWrapper(ex.getMessage());
  }


}
