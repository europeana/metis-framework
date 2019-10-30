package eu.europeana.metis.dereference.rest.exceptions;

import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
    HttpStatus status = AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class)
        .value();
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
