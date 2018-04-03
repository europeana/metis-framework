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
  }

  public Date getFirstPublishedDate() {
    return firstPublishedDate;
  }

  public void setFirstPublishedDate(Date firstPublishedDate) {
    this.firstPublishedDate = firstPublishedDate;
  }

  public Date getLastPublishedDate() {
    return lastPublishedDate;
  }

  public void setLastPublishedDate(Date lastPublishedDate) {
    this.lastPublishedDate = lastPublishedDate;
  }

  public int getLastPublishedRecords() {
    return lastPublishedRecords;
  }

  public void setLastPublishedRecords(int lastPublishedRecords) {
    this.lastPublishedRecords = lastPublishedRecords;
  }

  public Date getLastHarvestedDate() {
    return lastHarvestedDate;
  }

  public void setLastHarvestedDate(Date lastHarvestedDate) {
    this.lastHarvestedDate = lastHarvestedDate;
  }

  public int getLastHarvestedRecords() {
    return lastHarvestedRecords;
  }

  public void setLastHarvestedRecords(int lastHarvestedRecords) {
    this.lastHarvestedRecords = lastHarvestedRecords;
  }
}
