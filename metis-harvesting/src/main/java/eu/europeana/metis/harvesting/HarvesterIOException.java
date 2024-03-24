package eu.europeana.metis.harvesting;

import java.io.IOException;
import java.io.Serial;

/**
 * This exception represents any IO issue that can occur while trying to harvest records.
 */
public class HarvesterIOException extends IOException {

  @Serial
  private static final long serialVersionUID = 7581296997594994366L;

  /**
   * Constructor.
   *
   * @param message The message of the exception.
   * @param cause The cause of the exception.
   */
  public HarvesterIOException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor.
   *
   * @param message The message of the exception.
   */
  public HarvesterIOException(String message) {
    super(message);
  }
}
