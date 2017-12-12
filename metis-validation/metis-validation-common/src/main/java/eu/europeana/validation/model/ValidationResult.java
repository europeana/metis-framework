/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
