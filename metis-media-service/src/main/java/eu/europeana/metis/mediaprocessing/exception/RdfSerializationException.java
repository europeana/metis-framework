package eu.europeana.metis.mediaprocessing.exception;

public class RdfSerializationException extends Exception {

  public RdfSerializationException(String message) {
    super(message);
  }

  public RdfSerializationException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
