package eu.europeana.validation.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Validation result bean
 * Created by ymamakis on 12/22/15.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value="Validation Result",description = "Single service result")
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
     * Id of a node for which error occured. Null if success
     */
    @XmlElement
    @ApiModelProperty(value = "The node identifier message", required = false)
    private String nodeId;

    /**
     * The service result. true if success, false if failure
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

    public String getNodeId() { return nodeId; }

    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
