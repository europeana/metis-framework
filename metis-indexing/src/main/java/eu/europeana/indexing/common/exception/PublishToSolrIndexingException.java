package eu.europeana.indexing.common.exception;

import java.io.Serial;

public class PublishToSolrIndexingException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -276105897439704602L;

  public PublishToSolrIndexingException(String message, Exception cause) { super(message, cause);}
}
