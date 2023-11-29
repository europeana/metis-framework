package eu.europeana.metis.exception;

/**
 * Type of ExternalTaskException indicating that there is no sense of retrying failed operation.
 * Because the result will be always negative. For example when the task does not exist on server.
 */
public class UnrecoverableExternalTaskException extends ExternalTaskException{

  public UnrecoverableExternalTaskException(String message, Throwable cause) {
    super(message,cause);
  }
}
