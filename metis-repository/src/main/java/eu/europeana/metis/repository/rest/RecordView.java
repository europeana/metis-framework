package eu.europeana.metis.repository.rest;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;

public class RecordView {

  private final String recordId;
  private final String datasetId;
  @JsonSerialize(using = InstantSerializer.class)
  private final Instant dateStamp;
  @JsonRawValue
  private final String edmRecord;

  public RecordView(String recordId, String datasetId, Instant dateStamp, String edmRecord) {
    this.recordId = recordId;
    this.datasetId = datasetId;
    this.dateStamp = dateStamp;
    this.edmRecord = edmRecord;
  }

  public String getRecordId() {
    return recordId;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public Instant getDateStamp() {
    return dateStamp;
  }

  public String getEdmRecord() {
    return edmRecord;
  }
}
