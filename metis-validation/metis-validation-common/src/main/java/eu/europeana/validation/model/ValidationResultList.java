package eu.europeana.validation.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper class for the batch service Created by ymamakis on 12/22/15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value = "Validation Result List", description = "Batch service result")
public class ValidationResultList implements Serializable {

  private static final long serialVersionUID = 1905122041950251207L;

  /**
   * List of service results. If the list is empty then we assume success == true
   */
  @XmlElement
  @ApiModelProperty(value = "Result list")
  private List<ValidationResult> resultList;

  /**
   * The result of the batch service
   */
  @XmlElement
  @ApiModelProperty(value = "Operation outcome", required = true)
  private boolean success;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public List<ValidationResult> getResultList() {
    return resultList == null ? null : new ArrayList<>(resultList);
  }

  public void setResultList(List<ValidationResult> resultList) {
    this.resultList = resultList == null ? null : new ArrayList<>(resultList);
  }
}
