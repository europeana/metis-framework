package eu.europeana.metis.mediaprocessing.exception;

public class MediaExtractionException extends Exception {

  public MediaExtractionException(String message) {
    super(message);
  }

  public MediaExtractionException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
