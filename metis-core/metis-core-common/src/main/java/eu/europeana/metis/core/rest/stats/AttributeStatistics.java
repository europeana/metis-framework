package eu.europeana.metis.core.rest.stats;

/**
 * Statistics object that reflect the attribute level: statistics cover all attributes of this
 * name/path, within nodes with the same xPath and the same node value.
 */
public class AttributeStatistics {

  private String xPath;
  private String value;
  private long occurrences;

  public String getxPath() {
    return xPath;
  }

  public void setxPath(String xPath) {
    this.xPath = xPath;
  }

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
}
