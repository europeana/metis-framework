package eu.europeana.metis.data.checker.common.exception;


import eu.europeana.metis.data.checker.common.model.ExtendedValidationResult;

/**
 * Created by ymamakis on 9/29/16.
 */
public class DataCheckerValidationException extends Exception {

  private final ExtendedValidationResult result;

  public DataCheckerValidationException(ExtendedValidationResult result) {
    this.result = result;
  }

  public ExtendedValidationResult getValidationResult() {
    return this.result;
  }
}
