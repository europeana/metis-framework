package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-30
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "Plugin Execution Not Allowed")
public class PluginExecutionNotAllowed extends Exception {

  private static final long serialVersionUID = -3332292346834265371L;

  public PluginExecutionNotAllowed(String message) {
    super(message);
  }

}