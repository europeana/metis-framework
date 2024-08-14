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
   *
   * @param depublishRecordId The depublishRecordId to create this view for.
   */
  public DepublishRecordIdView(DepublishRecordId depublishRecordId) {
    this.recordId = depublishRecordId.getRecordId();
    this.depublicationDate = depublishRecordId.getDepublicationDate();
    this.depublicationStatus = DepublicationStatus
        .convertFromModelToView(depublishRecordId.getDepublicationStatus());
    this.depublicationReason =
        depublishRecordId.getDepublicationReason() == null ? DepublicationReason.UNKNOWN.toString()
            : depublishRecordId.getDepublicationReason().toString();
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

    private static DepublicationStatus convertFromModelToView(DepublishRecordId.DepublicationStatus depublicationStatus) {
      return switch (depublicationStatus) {
        case DEPUBLISHED -> DepublicationStatus.DEPUBLISHED;
        case PENDING_DEPUBLICATION -> DepublicationStatus.PENDING;
        case null -> null;
      };
    }
  }
}
