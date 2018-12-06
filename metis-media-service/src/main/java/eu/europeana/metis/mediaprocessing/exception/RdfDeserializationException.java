package eu.europeana.metis.mediaprocessing.exception;

public class RdfDeserializationException extends Exception {

  public RdfDeserializationException(String message) {
    super(message);
  }

  public RdfDeserializationException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
