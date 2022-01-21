package eu.europeana.indexing.tiers.view;

/**
 * Class containing information about an error.
 */
public class ProcessingError {

  private final String errorMessage;
  private final String stackTrace;

  /**
   * Constructor with required parameters.
   *
   * @param errorMessage the error message
   * @param stackTrace the stack trace
   */
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
