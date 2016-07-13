package eu.europeana.validation.edm.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Wrapper class for the batch validation
 * Created by ymamakis on 12/22/15.
 */
@XmlRootElement
@ApiModel(value="Validation Result List",description = "Batch validation result")
public class ValidationResultList {

    /**
     * List of validation results. If the list is empty then we assume success == true
     */
    @XmlElement
    @ApiModelProperty(value ="Result list",required = false)
    private List<ValidationResult> resultList;

    /**
     * The result of the batch validation
     */
    @XmlElement
    @ApiModelProperty(value ="Operation outcome",required = true)
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ValidationResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<ValidationResult> resultList) {
        this.resultList = resultList;
    }
}
