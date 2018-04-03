package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class DatasetExecutionInformation {
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date firstPublishedDate;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date lastPublishedDate;
  private int lastPublishedRecords;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date lastHarvestedDate;
  private int lastHarvestedRecords;

  public DatasetExecutionInformation() {
    //Required for json serialization
  }

  public Date getFirstPublishedDate() {
    return firstPublishedDate == null?null:new Date(firstPublishedDate.getTime());
  }

  public void setFirstPublishedDate(Date firstPublishedDate) {
    this.firstPublishedDate = firstPublishedDate == null?null:new Date(firstPublishedDate.getTime());
  }

  public Date getLastPublishedDate() {
    return lastPublishedDate == null?null:new Date(lastPublishedDate.getTime());
  }

  public void setLastPublishedDate(Date lastPublishedDate) {
    this.lastPublishedDate = lastPublishedDate == null?null:new Date(lastPublishedDate.getTime());
  }

  public int getLastPublishedRecords() {
    return lastPublishedRecords;
  }

  public void setLastPublishedRecords(int lastPublishedRecords) {
    this.lastPublishedRecords = lastPublishedRecords;
  }

  public Date getLastHarvestedDate() {
    return lastHarvestedDate == null?null:new Date(lastHarvestedDate.getTime());
  }

  public void setLastHarvestedDate(Date lastHarvestedDate) {
    this.lastHarvestedDate = lastHarvestedDate == null?null:new Date(lastHarvestedDate.getTime());
  }

  public int getLastHarvestedRecords() {
    return lastHarvestedRecords;
  }

  public void setLastHarvestedRecords(int lastHarvestedRecords) {
    this.lastHarvestedRecords = lastHarvestedRecords;
  }
}
