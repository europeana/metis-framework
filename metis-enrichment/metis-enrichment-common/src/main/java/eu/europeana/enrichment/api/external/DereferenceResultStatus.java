package eu.europeana.enrichment.api.external;

/**
 * Dereference result status
 */
public enum DereferenceResultStatus {
  SUCCESS,
  INVALID_URL,
  NO_VOCABULARY_MATCHING,
  NO_ENTITY_FOR_VOCABULARY,
  ENTITY_FOUND_XML_XSLT_ERROR,
  ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS,
  UNKNOWN_ENTITY
}
