package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;

/**
 * Dereferenced entity with status
 */
public class MongoDereferencedEntity {

  private final String entity;
  private final DereferenceResultStatus dereferenceResultStatus;

  /**
   * Constructor for entity and status
   * @param entity dereferenced entity
   * @param dereferenceResultStatus status of the entity
   */
  public MongoDereferencedEntity(String entity, DereferenceResultStatus dereferenceResultStatus) {
    this.entity = entity;
    this.dereferenceResultStatus = dereferenceResultStatus;
  }

  public String getEntity() {
    return entity;
  }

  public DereferenceResultStatus getDereferenceResultStatus() {
    return dereferenceResultStatus;
  }
}
