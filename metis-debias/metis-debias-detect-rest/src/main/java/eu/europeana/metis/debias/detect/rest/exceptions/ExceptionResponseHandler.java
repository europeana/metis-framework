package eu.europeana.metis.debias.detect.rest.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
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
}
