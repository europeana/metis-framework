package eu.europeana.metis.debias.detect.model.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.metis.debias.detect.model.DeBiasResult;
import java.util.Collections;
import java.util.List;

/**
 * The type Error result.
 */
public class ErrorDeBiasResult implements DeBiasResult {

  @JsonProperty("detail")
  private List<Detail> detailList;

  /**
   * Gets detail list.
   *
   * @return the detail list
   */
  public List<Detail> getDetailList() {
    return detailList;
  }

  /**
   * Sets detail list.
   *
   * @param detailList the detail list
   */
  public void setDetailList(List<Detail> detailList) {
    if (detailList != null) {
      this.detailList = Collections.unmodifiableList(detailList);
    }
  }
}
