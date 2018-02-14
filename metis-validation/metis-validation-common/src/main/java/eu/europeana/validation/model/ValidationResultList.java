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
import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class for the batch service
 * Created by ymamakis on 12/22/15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value="Validation Result List",description = "Batch service result")
public class ValidationResultList implements Serializable{

    private static final long serialVersionUID = -8670240754211591813L;

    /**
     * List of service results. If the list is empty then we assume success == true
     */
    @XmlElement
    @ApiModelProperty(value ="Result list",required = false)
    private List<ValidationResult> resultList;

    /**
     * The result of the batch service
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
