package eu.europeana.metis.core.rest;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-23
 */
public class Record {
  private String ecloudId;
  private String xmlRecord;

  public Record() {
    //Required for json serialization
  }

  /**
   * Constructore with the required arguments
   * @param ecloudId the ecloudId representing the record
   * @param xmlRecord the text representing the xml record
   */
  public Record(String ecloudId, String xmlRecord) {
    this.ecloudId = ecloudId;
    this.xmlRecord = xmlRecord;
  }

  public String getEcloudId() {
    return ecloudId;
  }

  public void setEcloudId(String ecloudId) {
    this.ecloudId = ecloudId;
  }

  public String getXmlRecord() {
    return xmlRecord;
  }

  public void setXmlRecord(String xmlRecord) {
    this.xmlRecord = xmlRecord;
  }
}
