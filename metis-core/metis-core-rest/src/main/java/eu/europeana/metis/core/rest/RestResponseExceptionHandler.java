package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.exceptions.ApiKeyNotAuthorizedException;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.exceptions.StructuredExceptionWrapper;
import eu.europeana.metis.core.exceptions.UserNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@ControllerAdvice
public class RestResponseExceptionHandler {

  @ExceptionHandler(value = {UserNotFoundException.class, ApiKeyNotAuthorizedException.class, NoApiKeyFoundException.class, IOException.class,
      SolrServerException.class, OrganizationAlreadyExistsException.class,
      NoOrganizationFoundException.class, BadContentException.class})
  @ResponseBody
  public StructuredExceptionWrapper handleException(HttpServletRequest request, Exception ex, HttpServletResponse response) {
    HttpStatus status = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class).value();
    response.setStatus(status.value());
    return new StructuredExceptionWrapper(ex.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper("Message body not readable. It is missing or malformed\n" + ex.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseBody
  public StructuredExceptionWrapper handleMissingParams(MissingServletRequestParameterException ex, HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return new StructuredExceptionWrapper(ex.getParameterName() + " parameter is missing");
  }
}
