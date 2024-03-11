package eu.europeana.metis.dereference.rest.exceptions;

import java.net.URISyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Generic Exception Handler Created by ymamakis on 2/25/16.
 **/
@ControllerAdvice
public class RestResponseExceptionHandler{

  @ResponseBody
  @ExceptionHandler({Exception.class})
  public ServerError handleResponse(HttpServletResponse response, HttpServletRequest req,
      Exception exception) {
    final ResponseStatus annotationResponseStatus = AnnotationUtils
        .findAnnotation(exception.getClass(), ResponseStatus.class);
    HttpStatus status = annotationResponseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR
        : annotationResponseStatus.value();
    response.setStatus(status.value());
    return new ServerError(exception.getMessage());
  }

  @ResponseBody
  @ExceptionHandler({URISyntaxException.class})
  public ServerError handleResponseURISystax(HttpServletResponse response, HttpServletRequest req,
      Exception exception) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    return new ServerError(exception.getMessage());
  }

  @XmlRootElement(name = "error")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class ServerError{

    private String errorMessage;

    public ServerError() {
    }

    public ServerError(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

}
