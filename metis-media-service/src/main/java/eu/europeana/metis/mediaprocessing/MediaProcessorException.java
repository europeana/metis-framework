package eu.europeana.metis.mediaprocessing;

public class MediaProcessorException extends Exception {

  public MediaProcessorException(String message) {
    super(message);
  }

  public MediaProcessorException(String message, Exception cause) {
    super(message, cause);
  }
}
