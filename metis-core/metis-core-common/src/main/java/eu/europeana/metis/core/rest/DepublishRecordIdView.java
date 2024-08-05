package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.common.JavaTimeSerialization.IsoInstantSerializer;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.workflow.plugins.DepublicationReason;
import java.time.Instant;

/**
 * An immutable view on the depublish record id.
 */
public class DepublishRecordIdView {

  private final String recordId;
  private final DepublicationStatus depublicationStatus;
  private final String depublicationReason;

  @JsonSerialize(using = IsoInstantSerializer.class)
  private final Instant depublicationDate;

  /**
   * Constructor.
   * @param record The record to create this view for.
   */
  public DepublishRecordIdView(DepublishRecordId record) {
    this.recordId = record.getRecordId();
    this.depublicationDate = record.getDepublicationDate();
    this.depublicationStatus = DepublicationStatus
        .convertFromModelToView(record.getDepublicationStatus());
    this.depublicationReason = record.getDepublicationReason() == null ? DepublicationReason.UNKNOWN.toString() :
        record.getDepublicationReason().toString();
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

  public String getDepublicationReason() {
    return depublicationReason;
  }

  /**
   * The status of this record with regards to (de)publication.
   */
  public enum DepublicationStatus {
    DEPUBLISHED, PENDING;

    private static DepublicationStatus convertFromModelToView(
        DepublishRecordId.DepublicationStatus depublicationStatus) {
      DepublicationStatus depublicationStatusView = null;
      if (depublicationStatus != null) {
        switch (depublicationStatus) {
          case DEPUBLISHED -> depublicationStatusView = DepublicationStatus.DEPUBLISHED;
          default -> depublicationStatusView = DepublicationStatus.PENDING;
        }
      }
      return depublicationStatusView;
    }
  }
}
