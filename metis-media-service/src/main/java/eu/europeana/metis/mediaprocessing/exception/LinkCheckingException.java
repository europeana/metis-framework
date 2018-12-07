package eu.europeana.metis.mediaprocessing.exception;

public class LinkCheckingException extends Exception {

  public LinkCheckingException(String message) {
    super(message);
  }

  public LinkCheckingException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public LinkCheckingException(Throwable throwable) {
    super(throwable);
  }
}
