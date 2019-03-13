package eu.europeana.metis.core.rest.stats;

public class AttributeStatistics {

  private String xPath;
  private String value;
  private long occurrence;

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

  public long getOccurrence() {
    return occurrence;
  }

  public void setOccurrence(long occurrence) {
    this.occurrence = occurrence;
  }
}
