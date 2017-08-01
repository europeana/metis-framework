package eu.europeana.metis.preview.common.exception;


import eu.europeana.metis.preview.common.model.ExtendedValidationResult;

/**
 * Created by ymamakis on 9/29/16.
 */
public class PreviewValidationException extends Exception {
    private ExtendedValidationResult result;
    public PreviewValidationException(ExtendedValidationResult result){
        super();
        this.result = result;
    }

    public ExtendedValidationResult getValidationResult(){
        return this.result;
    }
}
