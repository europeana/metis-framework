package eu.europeana.metis.repository.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class InsertionResult {

  private final String datasetId;
  @JsonSerialize(using = InstantSerializer.class)
  private final Instant dateStamp;
  private int insertedRecords = 0;
  private int updatedRecords = 0;
  private final Set<String> insertedRecordIds = new HashSet<>();
  private final Set<String> updatedRecordIds = new HashSet<>();

  public InsertionResult(String datasetId, Instant dateStamp) {
    this.datasetId = datasetId;
    this.dateStamp = dateStamp;
  }

  public void addInsertedRecord(String recordId) {
    if (insertedRecordIds.add(recordId)) {
      insertedRecords++;
    }
  }

  public void addUpdatedRecord(String recordId) {
    if (updatedRecordIds.add(recordId)) {
      updatedRecords++;
    }
  }

  public String getDatasetId() {
    return datasetId;
  }

  public Instant getDateStamp() {
    return dateStamp;
  }

  public int getInsertedRecords() {
    return insertedRecords;
  }

  public int getUpdatedRecords() {
    return updatedRecords;
  }

  public Set<String> getInsertedRecordIds() {
    return Collections.unmodifiableSet(insertedRecordIds);
  }

  public Set<String> getUpdatedRecordIds() {
    return Collections.unmodifiableSet(updatedRecordIds);
  }
}
