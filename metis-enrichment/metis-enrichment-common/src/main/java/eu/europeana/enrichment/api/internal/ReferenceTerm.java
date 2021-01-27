package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.Set;

/**
 * Represents a reference term: a URL reference in a record with a list of candidate reference
 * types.
 */
public interface ReferenceTerm {

  Set<EntityType> getCandidateTypes();

  URL getReference();
}
