package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * Dataset search model that contains all the required fields for Dataset Search functionality.
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2019-11-12
 */
public class DatasetSearchView {

  private String datasetId;
  private String datasetName;
  private String provider;
  private String dataProvider;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date lastExecutionDate;

  public DatasetSearchView() {
    //Required for json (de)serialization
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getDatasetName() {
    return datasetName;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getDataProvider() {
    return dataProvider;
  }

  public void setDataProvider(String dataProvider) {
    this.dataProvider = dataProvider;
  }

  public Date getLastExecutionDate() {
    return lastExecutionDate == null ? null : new Date(lastExecutionDate.getTime());
  }

  public void setLastExecutionDate(Date lastExecutionDate) {
    this.lastExecutionDate =
        lastExecutionDate == null ? null : new Date(lastExecutionDate.getTime());
  }
}
