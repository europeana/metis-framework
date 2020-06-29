package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.common.JavaTimeSerialization.IsoInstantSerializer;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.json.ObjectIdSerializer;
import eu.europeana.metis.mongo.HasMongoObjectId;
import java.time.Instant;
import org.bson.types.ObjectId;

public class DepublishedRecordView implements HasMongoObjectId {

  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private final String recordId;
  private final DepublicationStatus depublicationStatus;

  @JsonSerialize(using = IsoInstantSerializer.class)
  private final Instant depublicationDate;

  public DepublishedRecordView(DepublishRecordId record) {
    this.id = record.getId();
    this.recordId = record.getRecordId();
    this.depublicationDate = record.getDepublicationDate();
    this.depublicationStatus = DepublicationStatus
        .convertFromModelToView(record.getDepublicationStatus());
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


  public enum DepublicationStatus {
    DEPUBLISHED, PENDING;

    private static DepublicationStatus convertFromModelToView(
        DepublishRecordId.DepublicationStatus depublicationStatus) {
      DepublicationStatus depublicationStatusView = null;
      if (depublicationStatus != null) {
        switch (depublicationStatus) {
          case DEPUBLISHED:
            depublicationStatusView = DepublicationStatus.DEPUBLISHED;
            break;
          case PENDING_DEPUBLICATION:
          default:
            depublicationStatusView = DepublicationStatus.PENDING;
        }
      }
      return depublicationStatusView;
    }
  }
}
