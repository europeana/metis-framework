package eu.europeana.metis.preview.service.executor;

import eu.europeana.validation.model.ValidationResult;

public class ValidationTaskResult {

  private String recordId;
  private ValidationResult validationResult;
  private boolean success;

  public String getRecordId() {
    return recordId;
  }

  public ValidationResult getValidationResult() {
    return validationResult;
  }

  public boolean isSuccess() {
    return success;
  }

  public ValidationTaskResult(String recordId, ValidationResult validationResult, boolean success) {
    this.recordId = recordId;
    this.validationResult = validationResult;
    this.success = success;
  }
}
