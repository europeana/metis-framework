package eu.europeana.metis.debias.detect.rest.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.net.URISyntaxException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionResponseHandler {

  @ResponseBody
  @ExceptionHandler({Exception.class})
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request,
      Exception exception) {
    final ResponseStatus annotationResponseStatus = AnnotationUtils
        .findAnnotation(exception.getClass(), ResponseStatus.class);
    HttpStatus status = annotationResponseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR
        : annotationResponseStatus.value();
    response.setStatus(status.value());
    return new ServerError(exception.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request, HttpMediaTypeNotSupportedException exception) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return new ServerError(exception.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest request, HttpMessageNotReadableException exception) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return new ServerError(exception.getMessage());
  }

  @ResponseBody
  @ExceptionHandler({URISyntaxException.class})
  public ServerError handleResponseURISystax(HttpServletResponse response, HttpServletRequest req,
      Exception exception) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return new ServerError(exception.getMessage());
  }
}
