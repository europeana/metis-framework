package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.common.JavaTimeSerialization.IsoInstantSerializer;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import java.time.Instant;

public class DepublishRecordIdView {

  private final String recordId;
  private final DepublicationStatus depublicationStatus;

  @JsonSerialize(using = IsoInstantSerializer.class)
  private final Instant depublicationDate;

  public DepublishRecordIdView(DepublishRecordId record) {
    this.recordId = record.getRecordId();
    this.depublicationDate = record.getDepublicationDate();
    this.depublicationStatus = DepublicationStatus
        .convertFromModelToView(record.getDepublicationStatus());
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
