package eu.europeana.enrichment.rest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EnrichmentExceptionTest {

  private static final String ERROR_MESSAGE = "error message";

  @Test
  void testConstructorWithMessage(){
    EnrichmentException exception = new EnrichmentException(ERROR_MESSAGE);
    assertEquals(ERROR_MESSAGE, exception.getMessage());
  }

  @Test
  void testConstructorWithMessageAndCause(){
    Throwable throwable = new Throwable(ERROR_MESSAGE);
    EnrichmentException exception = new EnrichmentException(ERROR_MESSAGE, throwable);

    assertEquals(ERROR_MESSAGE, exception.getMessage());
    assertEquals(throwable, exception.getCause());
    assertEquals(ERROR_MESSAGE, exception.getCause().getMessage());
  }

}
