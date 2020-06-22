package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.common.JavaTimeSerialization.IsoInstantSerializer;
import eu.europeana.metis.core.dataset.DepublishedRecord;
import eu.europeana.metis.core.dataset.DepublishedRecord.DepublicationStatus;
import eu.europeana.metis.json.ObjectIdSerializer;
import eu.europeana.metis.mongo.HasMongoObjectId;
import java.time.Instant;
import org.bson.types.ObjectId;

public class DepublishedRecordView implements HasMongoObjectId {

  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private String recordId;
  private DepublicationStatus depublicationStatus;

  @JsonSerialize(using = IsoInstantSerializer.class)
  private Instant depublicationDate;

  public DepublishedRecordView(DepublishedRecord record) {
    this.id = record.getId();
    this.recordId = record.getRecordId();
    this.depublicationDate = record.getDepublicationDate();
    this.depublicationStatus = record.getDepublicationStatus();
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  public String getRecordId() {
    return recordId;
  }

  public DepublicationStatus getDepublicationStatus() {
    return depublicationStatus;
  }

  public Instant getDepublicationDate() {
    return depublicationDate;
  }
}
