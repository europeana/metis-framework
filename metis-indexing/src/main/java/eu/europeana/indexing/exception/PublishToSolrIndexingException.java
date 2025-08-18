package eu.europeana.indexing.exception;

import java.io.Serial;

/**
 * Exception for issues with publishing data to Solr.
 */
public class PublishToSolrIndexingException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -276105897439704602L;

  /**
   * Constructor
   *
   * @param message The message.
   * @param cause The cause.
   */
  public PublishToSolrIndexingException(String message, Exception cause) { super(message, cause);}
}
