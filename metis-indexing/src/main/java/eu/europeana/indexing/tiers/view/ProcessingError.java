package eu.europeana.indexing.tiers.view;

public class ProcessingError {

  private String errorMessage;
  private int errorCode;

  public ProcessingError() {
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }
}
