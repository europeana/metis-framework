package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class can perform enrichment.
 */
public interface Enricher {

  /**
   * Performs the enrichment procedure on a RDF
   *
   * @param rdf The RDF to be processed
   * @throws EnrichmentException In case that something unexpected happened.
   */
  void enrichment(final RDF rdf) throws EnrichmentException;

  /**
   * Retrieves information to enrich the RDF using extracted values
   *
   * @param searchTerms The values extracted to enrich the RDF
   * @return For each search term a list with entities retrieved.
   * @throws EnrichmentException In case that something unexpected happened.
   */
  Map<SearchTermContext, List<EnrichmentBase>> enrichValues(Set<SearchTermContext> searchTerms)
      throws EnrichmentException;

  /**
   * Retrieves information to enrich the RDF using extracted references
   *
   * @param references The references extracted to enrich the RDF
   * @return For each reference a list with entities retrieved.
   * @throws EnrichmentException In case that something unexpected happened.
   */
  Map<ReferenceTermContext, List<EnrichmentBase>> enrichReferences(
      Set<ReferenceTermContext> references) throws EnrichmentException;
}
