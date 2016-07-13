package eu.europeana.validation.edm.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Validation result bean
 * Created by ymamakis on 12/22/15.
 */

@XmlRootElement
@ApiModel(value="Validation Result",description = "Single validation result")
public class ValidationResult {

    /**
     * The record id that generated the issue. Null if success
     */
    @XmlElement
    @ApiModelProperty(value = "The record identifier", required = false)
    private String recordId;

    /**
     * The error code. Null if success
     */
    @XmlElement
    @ApiModelProperty(value = "The error message", required = false)
    private String message;

    /**
     * The validation result. true if success, false if failure
     */
    @ApiModelProperty(value = "Failed or successful operation", required = true)
    @XmlElement
    private boolean success;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
