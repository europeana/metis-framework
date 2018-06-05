package eu.europeana.metis.data.checker.service.executor;

import eu.europeana.validation.model.ValidationResult;

public class ValidationTaskResult {

  private final String recordId;
  private final ValidationResult validationResult;
  private final boolean success;

  public ValidationTaskResult(String recordId, ValidationResult validationResult, boolean success) {
    this.recordId = recordId;
    this.validationResult = validationResult;
    this.success = success;
  }

  public String getRecordId() {
    return recordId;
  }

  public ValidationResult getValidationResult() {
    return validationResult;
  }

  public boolean isSuccess() {
    return success;
  }
}
