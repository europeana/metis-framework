package eu.europeana.indexing.exception;

public class PublishToSolrIndexingException extends RuntimeException {

  private static final long serialVersionUID = -276105897439704602L;

  public PublishToSolrIndexingException(String message, Exception cause) { super(message, cause);}
}
