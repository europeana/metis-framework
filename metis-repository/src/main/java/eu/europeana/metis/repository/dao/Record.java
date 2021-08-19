package eu.europeana.metis.repository.dao;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import java.time.Instant;
import org.bson.types.ObjectId;

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
  private String edmRecord;

  public Record() {
    // Empty constructor required for Morphia.
  }

  public Record(String recordId, String datasetId, Instant dateStamp, String edmRecord) {
    this.recordId = recordId;
    this.datasetId = datasetId;
    this.dateStamp = dateStamp;
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

  public String getEdmRecord() {
    return edmRecord;
  }

  public void setEdmRecord(String edmRecord) {
    this.edmRecord = edmRecord;
  }
}
