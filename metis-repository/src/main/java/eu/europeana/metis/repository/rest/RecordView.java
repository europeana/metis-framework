package eu.europeana.metis.repository.rest;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;

public class RecordView {

  private final String recordId;
  private final String datasetId;
  @JsonSerialize(using = InstantSerializer.class)
  private final Instant dateStamp;
  private final boolean markedAsDeleted;
  @JsonRawValue
  private final String edmRecord;

  public RecordView(String recordId, String datasetId, Instant dateStamp, boolean markedAsDeleted,
      String edmRecord) {
    this.recordId = recordId;
    this.datasetId = datasetId;
    this.dateStamp = dateStamp;
    this.edmRecord = edmRecord;
    this.markedAsDeleted = markedAsDeleted;
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

  public boolean isMarkedAsDeleted() {
    return markedAsDeleted;
  }

  public String getEdmRecord() {
    return edmRecord;
  }
}
