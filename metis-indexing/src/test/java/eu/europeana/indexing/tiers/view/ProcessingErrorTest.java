package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProcessingErrorTest {

  @Test
  void objectCreationTest() {
    final String errorMessage = "errorMessage";
    final String stackTrace = "stackTrace\nAnother line";
    final ProcessingError processingError = new ProcessingError(errorMessage, stackTrace);
    assertEquals(errorMessage, processingError.getErrorMessage());
    assertEquals(stackTrace, processingError.getStackTrace());
  }

}