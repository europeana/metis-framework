package eu.europeana.indexing.tiers.view;

public class ProcessingError {

  private final String errorMessage;
  private final String stackTrace;

  public ProcessingError(String errorMessage, String stackTrace) {
    this.errorMessage = errorMessage;
    this.stackTrace = stackTrace;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getStackTrace() {
    return stackTrace;
  }
}
