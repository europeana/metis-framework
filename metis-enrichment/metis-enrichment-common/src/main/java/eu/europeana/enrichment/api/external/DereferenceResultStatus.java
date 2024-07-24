package eu.europeana.enrichment.api.external;

import java.net.URI;

/**
 * Dereference result status
 */
public enum DereferenceResultStatus {

  /**
   * This means that dereferencing was done successfully.
   */
  SUCCESS,

  /**
   * This means that the resource ID to dereference is not valid (see {@link URI#URI(String)}).
   */
  INVALID_URL,

  /**
   * This means that no vocabulary is known that matches to the resource ID.
   */
  NO_VOCABULARY_MATCHING,

  /**
   * This means that one or more vocabularies could be found matching the resource ID, but no
   * entity is known by that ID at source.
   */
  NO_ENTITY_FOR_VOCABULARY,

  /**
   * This means that the source entity was obtained, but an error occurred either when transforming
   * it to a contextual class, or when parsing the result of that transformation.
   */
  ENTITY_FOUND_XML_XSLT_ERROR,

  /**
   * This means that the source entity was obtained, but the transformation yielded no resulting
   * contextual class (probably by design: the entity was determined not to qualify by the
   * transformation).
   */
  ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS,

  /**
   * This means that an entity ID corresponding to the Europeana entity collection was provided,
   * but no entity is known by that ID.
   */
  UNKNOWN_EUROPEANA_ENTITY,

  /**
   * This means an unspecified failure: this should be reported as a bug.
   */
  FAILURE
}
