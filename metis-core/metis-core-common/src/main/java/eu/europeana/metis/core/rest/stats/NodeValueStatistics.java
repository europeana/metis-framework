package eu.europeana.metis.core.rest.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeValueStatistics {

  private String value;
  private long occurrences;
  private List<AttributeStatistics> attributeStatistics;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public long getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(long occurrences) {
    this.occurrences = occurrences;
  }

  public List<AttributeStatistics> getAttributeStatistics() {
    return Collections.unmodifiableList(attributeStatistics);
  }

  public void setAttributeStatistics(
      List<AttributeStatistics> attributeStatistics) {
    this.attributeStatistics = new ArrayList<>(attributeStatistics);
  }
}
