package eu.europeana.validation.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Contains a list of validation results.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidationResultList implements Serializable {

  @Serial
  private static final long serialVersionUID = 1905122041950251207L;

  /**
   * List of service results. If the list is empty then we assume success == true
   */
  @XmlElement
  private List<ValidationResult> resultList;

  /**
   * The result of the batch service
   */
  @XmlElement
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
