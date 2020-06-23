package eu.europeana.metis.core.dataset;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.mongo.HasMongoObjectId;
import java.time.Instant;
import org.bson.types.ObjectId;

/**
 * A database model entity representing a depublished (or to-be-depublished) record belonging to a
 * dataset. The record is identified by an ID and can have a state and date of depublication.
 */
@Entity
@Indexes({
    @Index(fields = {@Field(DepublishedRecord.DATASET_ID_FIELD),
        @Field(DepublishedRecord.RECORD_ID_FIELD)}, options = @IndexOptions(unique = true)),
    @Index(fields = {@Field(DepublishedRecord.DATASET_ID_FIELD)}),
    @Index(fields = {@Field(DepublishedRecord.RECORD_ID_FIELD)})})
public class DepublishedRecord implements HasMongoObjectId {

  public static final String ID_FIELD = "_id";
  public static final String DATASET_ID_FIELD = "datasetId";
  public static final String RECORD_ID_FIELD = "recordId";
  public static final String DEPUBLICATION_STATE_FIELD = "depublicationStatus";
  public static final String DEPUBLICATION_DATE_FIELD = "depublicationDate";

  public enum DepublicationStatus {DEPUBLISHED, PENDING_DEPUBLICATION}

  /**
   * The ID of the data object.
   **/
  @Id
  private ObjectId id;

  /**
   * The dataset ID.
   **/
  private String datasetId;

  /**
   * The record ID (without dataset prefix).
   **/
  private String recordId;

  /**
   * The state of the record's depublication.
   **/
  private DepublicationStatus depublicationStatus;

  /**
   * The date of depublication.
   **/
  private Instant depublicationDate;

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public DepublicationStatus getDepublicationStatus() {
    return depublicationStatus;
  }

  public void setDepublicationStatus(DepublicationStatus depublicationStatus) {
    this.depublicationStatus = depublicationStatus;
  }

  public Instant getDepublicationDate() {
    return depublicationDate;
  }

  public void setDepublicationDate(Instant depublicationDate) {
    this.depublicationDate = depublicationDate;
  }
}
