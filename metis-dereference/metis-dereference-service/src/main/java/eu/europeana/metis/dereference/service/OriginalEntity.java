package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;

/**
 * Original entity as obtained from source, with status
 */
public class OriginalEntity {

  private final String entity;
  private final DereferenceResultStatus resultStatus;

  /**
   * Constructor for entity and status
   * @param entity dereferenced entity
   * @param resultStatus status of the entity
   */
  public OriginalEntity(String entity, DereferenceResultStatus resultStatus) {
    this.entity = entity;
    this.resultStatus = resultStatus;
  }

  public String getEntity() {
    return entity;
  }

  public DereferenceResultStatus getResultStatus() {
    return resultStatus;
  }
}
