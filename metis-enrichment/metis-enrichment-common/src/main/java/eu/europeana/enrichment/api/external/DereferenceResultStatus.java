package eu.europeana.enrichment.api.external;

/**
 * Enrichment result status
 */
public enum DereferenceResultStatus {
  SUCCESS,
  INVALID_URL,
  NO_VOCABULARY_MATCHING,
  NO_ENTITY_FOR_VOCABULARY,
  ENTITY_FOUND_XML_XLT_ERROR,
  UNKNOWN_ENTITY
}
