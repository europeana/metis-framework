package eu.europeana.metis.debias.detect.rest.exceptions;

import eu.europeana.metis.debias.detect.exceptions.DeBiasBadRequestException;
import eu.europeana.metis.debias.detect.exceptions.DeBiasInternalServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type Exception response handler.
 */
@ControllerAdvice
public class ExceptionResponseHandler {

  /**
   * Handle generic server error.
   *
   * @param response the response
   * @param request the request
   * @param exception the exception
   * @return the server error
   */
  @ResponseBody
  @ExceptionHandler({Exception.class})
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request,
      Exception exception) {
    final ResponseStatus annotationResponseStatus = AnnotationUtils
        .findAnnotation(exception.getClass(), ResponseStatus.class);
    HttpStatus status = annotationResponseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR
        : annotationResponseStatus.value();
    response.setStatus(status.value());
    return new ServerError(response.getStatus(), exception.getMessage());
  }

  /**
   * Handle Bad Request response server error.
   *
   * @param response the response
   * @param request the request
   * @param exception the exception
   * @return the server error
   */
  @ResponseBody
  @ExceptionHandler({DeBiasBadRequestException.class})
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request,
      DeBiasBadRequestException exception) {
    final ResponseStatus annotationResponseStatus = AnnotationUtils
        .findAnnotation(exception.getClass(), ResponseStatus.class);
    HttpStatus status = annotationResponseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR
        : annotationResponseStatus.value();
    response.setStatus(status.value());
    return new ServerError(response.getStatus(), exception.getMessage());
  }

  /**
   * Handle Internal Server response server error.
   *
   * @param response the response
   * @param request the request
   * @param exception the exception
   * @return the server error
   */
  @ResponseBody
  @ExceptionHandler({DeBiasInternalServerException.class})
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request,
      DeBiasInternalServerException exception) {
    final ResponseStatus annotationResponseStatus = AnnotationUtils
        .findAnnotation(exception.getClass(), ResponseStatus.class);
    HttpStatus status = annotationResponseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR
        : annotationResponseStatus.value();
    response.setStatus(status.value());
    return new ServerError(response.getStatus(), exception.getMessage());
  }

  /**
   * Handle media not supported response server error.
   *
   * @param response the response
   * @param request the request
   * @param exception the exception
   * @return the server error
   */
  @ResponseBody
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request,
      HttpMediaTypeNotSupportedException exception) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return new ServerError(response.getStatus(), exception.getMessage());
  }

  /**
   * Handle response server error.
   *
   * @param response the response
   * @param request the request
   * @param exception the exception
   * @return the server error
   */
  @ResponseBody
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request,
      HttpMessageNotReadableException exception) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return new ServerError(response.getStatus(), exception.getMessage());
  }
}
