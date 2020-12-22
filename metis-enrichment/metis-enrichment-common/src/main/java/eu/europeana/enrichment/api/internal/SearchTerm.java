package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.Set;

/**
 * Represents a search term: a text reference (with an optional language) in a record with a list of
 * candidate reference types.
 */
public interface SearchTerm {

  Set<EntityType> getCandidateTypes();

  String getTextValue();

  String getLanguage();
}
