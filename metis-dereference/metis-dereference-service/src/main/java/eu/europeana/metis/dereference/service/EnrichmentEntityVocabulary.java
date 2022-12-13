package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.metis.dereference.Vocabulary;

/**
 * Result of enrichment base or enrichment entity
 */
public class EnrichmentEntityVocabulary {

  private final EnrichmentBase enrichmentBase;
  private final Vocabulary vocabulary;
  private final String entity;
  private final DereferenceResultStatus dereferenceResultStatus;

  /**
   * Constructor for result including enrichment and vocabulary
   * @param enrichmentBase
   * @param vocabulary
   */
  public EnrichmentEntityVocabulary(EnrichmentBase enrichmentBase, Vocabulary vocabulary) {
    this.enrichmentBase = enrichmentBase;
    this.vocabulary = vocabulary;
    this.entity = null;
    this.dereferenceResultStatus = null;
  }

  /**
   * Constructor for result including enrichment, vocabulary and status
   * @param enrichmentBase
   * @param vocabulary
   * @param dereferenceResultStatus
   */
  public EnrichmentEntityVocabulary(EnrichmentBase enrichmentBase, Vocabulary vocabulary,
      DereferenceResultStatus dereferenceResultStatus) {
    this.enrichmentBase = enrichmentBase;
    this.vocabulary = vocabulary;
    this.dereferenceResultStatus = dereferenceResultStatus;
    this.entity = null;
  }

  /**
   * Constructor for including status
   * @param dereferenceResultStatus
   */
  public EnrichmentEntityVocabulary(DereferenceResultStatus dereferenceResultStatus) {
    this.enrichmentBase = null;
    this.vocabulary = null;
    this.entity = null;
    this.dereferenceResultStatus = dereferenceResultStatus;
  }

  /**
   * Constructor for including entity and vocabulary
   * @param entity
   * @param vocabulary
   */
  public EnrichmentEntityVocabulary(String entity, Vocabulary vocabulary) {
    this.entity = entity;
    this.vocabulary = vocabulary;
    this.enrichmentBase = null;
    this.dereferenceResultStatus = null;
  }

  /**
   * Constructor for including vocabulary and status
   * @param vocabulary
   * @param dereferenceResultStatus
   */
  public EnrichmentEntityVocabulary(Vocabulary vocabulary, DereferenceResultStatus dereferenceResultStatus) {
    this.enrichmentBase = null;
    this.vocabulary = vocabulary;
    this.entity = null;
    this.dereferenceResultStatus = dereferenceResultStatus;
  }

  /**
   *  Constructor for including entity and vocabulary
   * @param entity
   * @param vocabulary
   * @param dereferenceResultStatus
   */
  public EnrichmentEntityVocabulary(String entity, Vocabulary vocabulary, DereferenceResultStatus dereferenceResultStatus) {
    this.entity = entity;
    this.vocabulary = vocabulary;
    this.dereferenceResultStatus = dereferenceResultStatus;
    this.enrichmentBase = null;
  }

  public EnrichmentBase getEnrichmentBase() {
    return enrichmentBase;
  }

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  public String getEntity() {
    return entity;
  }

  public DereferenceResultStatus getDereferenceResultStatus() {
    return dereferenceResultStatus;
  }
}
