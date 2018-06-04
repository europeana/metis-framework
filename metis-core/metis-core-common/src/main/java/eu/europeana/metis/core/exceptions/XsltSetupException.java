package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * Exception used if an XSLT exists in the database but it could not be retrieved or parsed.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Bad xslt found.")
public class XsltSetupException extends GenericMetisException {

  /** This is an instance of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 3604852827523793668L;

  /**
   * Constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public XsltSetupException(String message, Exception cause) {
    super(message, cause);
  }
}
