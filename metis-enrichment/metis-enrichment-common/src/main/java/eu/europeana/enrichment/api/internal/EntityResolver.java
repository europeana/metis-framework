package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementations of this class have the ability to resolve entities based on the given input.
 */
public interface EntityResolver {

  /**
   * Resolve entities by a textual reference.
   *
   * @param searchTerms The search terms to resolve.
   * @return A map from the search term to a list of entities that the search term yielded.
   */
  Map<SearchTerm, List<EnrichmentBase>> resolveByText(Set<? extends SearchTerm> searchTerms);

  /**
   * Resolve entities by an ID reference (i.e. any resulting entity has the reference as ID).
   *
   * @param referenceTerms The references to resolve.
   * @return A map from the reference to the entity that the reference points to.
   */
  Map<ReferenceTerm, EnrichmentBase> resolveById(Set<? extends ReferenceTerm> referenceTerms);

  /**
   * Resolve entities by an equivalence reference (i.e. any resulting entity either has the
   * reference as ID or is defined to be equivalent to the reference).
   *
   * @param referenceTerms The references to resolve.
   * @return A map from the reference to a list of entities that the reference is equivalent to.
   */
  Map<ReferenceTerm, List<EnrichmentBase>> resolveByUri(
          Set<? extends ReferenceTerm> referenceTerms);
}
