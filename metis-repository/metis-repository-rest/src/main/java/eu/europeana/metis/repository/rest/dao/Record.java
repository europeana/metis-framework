package eu.europeana.metis.repository.rest.dao;

import dev.morphia.annotations.*;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import org.bson.types.ObjectId;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.Instant;

/**
 * Model (database) object representing a record. Apart from a (generated and immutable) ID, records
 * contain a record ID (could be used for instance as OAI identifier), a dataset ID (a String
 * value), a date stamp and the EDM record (XML) itself.
 */
@Entity
@Indexes({
        @Index(fields = {@Field("datasetId")}),
        @Index(fields = {@Field("recordId")}, options = @IndexOptions(unique = true)),
        @Index(fields = {@Field("datasetId"), @Field("recordId")})
})
public class Record {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  private String recordId;
  private String datasetId;
  private Instant dateStamp;
  private boolean deleted = false;
  private String edmRecord;

  public Record() {
    // Empty constructor required for Morphia.
  }

  public Record(String recordId, String datasetId, Instant dateStamp, boolean deleted,
      String edmRecord) {
    this.recordId = recordId;
    this.datasetId = datasetId;
    this.dateStamp = dateStamp;
    this.deleted = deleted;
    this.edmRecord = edmRecord;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public Instant getDateStamp() {
    return dateStamp;
  }

  public void setDateStamp(Instant dateStamp) {
    this.dateStamp = dateStamp;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public String getEdmRecord() {
    return edmRecord;
  }

  public void setEdmRecord(String edmRecord) {
    this.edmRecord = edmRecord;
  }
}
