package eu.europeana.metis.mediaprocessing.exception;

public class RdfConverterException extends Exception {

  public RdfConverterException(String message) {
    super(message);
  }

  public RdfConverterException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
