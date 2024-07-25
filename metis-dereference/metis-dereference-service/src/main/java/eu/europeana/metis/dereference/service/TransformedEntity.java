package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * Transformed entity, with vocabulary, original entity and status.
 */
public class TransformedEntity {

  private final Vocabulary vocabulary;
  private final String entity;
  private final DereferenceResultStatus resultStatus;

  /**
   * Constructor
   * @param vocabulary The vocabulary according to which this entity was transformed (can be null).
   * @param entity The result entity (can be null).
   * @param resultStatus The result status (should not be null).
   */
  public TransformedEntity(Vocabulary vocabulary, String entity,
      DereferenceResultStatus resultStatus) {
    this.vocabulary = vocabulary;
    this.entity = entity;
    this.resultStatus = resultStatus;
  }

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  public String getEntity() {
    return entity;
  }

  public DereferenceResultStatus getResultStatus() {
    return resultStatus;
  }
}
