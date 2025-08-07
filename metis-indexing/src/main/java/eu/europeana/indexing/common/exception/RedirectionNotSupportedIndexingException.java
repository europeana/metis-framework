package eu.europeana.indexing.common.exception;

import java.io.Serial;

/**
 * Exception indicating that a redirection related functionality was invoked on an indexer that
 * does not support redirection.
 */
public class RedirectionNotSupportedIndexingException extends IndexingException {

  @Serial
  private static final long serialVersionUID = -3700583844406879573L;

  public RedirectionNotSupportedIndexingException() {
    super("Redirection not supported.");
  }
}
