package eu.europeana.metis.data.checker.common.exception;


import eu.europeana.metis.data.checker.common.model.ExtendedValidationResult;

/**
 * Created by ymamakis on 9/29/16.
 */
public class DataCheckerValidationException extends Exception {
    private ExtendedValidationResult result;
    public DataCheckerValidationException(ExtendedValidationResult result){
        super();
        this.result = result;
    }

    public ExtendedValidationResult getValidationResult(){
        return this.result;
    }
}
