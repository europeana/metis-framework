package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * Transformed entity, with vocabulary, original entity and status.
 */
public class TransformedEntity {

  private final Vocabulary vocabulary;
  private final String transformedEntity;
  private final DereferenceResultStatus resultStatus;

  public TransformedEntity(Vocabulary vocabulary, String transformedEntity,
      DereferenceResultStatus resultStatus) {
    this.vocabulary = vocabulary;
    this.transformedEntity = transformedEntity;
    this.resultStatus = resultStatus;
  }

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  public String getTransformedEntity() {
    return transformedEntity;
  }

  public DereferenceResultStatus getResultStatus() {
    return resultStatus;
  }
}
