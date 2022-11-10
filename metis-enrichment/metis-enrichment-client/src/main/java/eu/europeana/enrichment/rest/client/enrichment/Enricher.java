package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Instances of this class can perform enrichment.
 */
public interface Enricher {

  /**
   * Performs the enrichment procedure on a RDF
   *
   * @param rdf The RDF to be processed
   * @return HashSet The messages encountered during enrichment
   */
  HashSet<ReportMessage> enrichment(final RDF rdf);

  /**
   * Retrieves information to enrich the RDF using extracted values
   *
   * @param searchTerms The values extracted to enrich the RDF
   * @return For each search term a list with entities retrieved.
   */
  Pair<Map<SearchTermContext, List<EnrichmentBase>>, HashSet<ReportMessage>> enrichValues(Set<SearchTermContext> searchTerms);

  /**
   * Retrieves information to enrich the RDF using extracted references
   *
   * @param references The references extracted to enrich the RDF
   * @return For each reference a list with entities retrieved.
   */
  Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, HashSet<ReportMessage>> enrichReferences(
      Set<ReferenceTermContext> references);

  /**
   * Cleanups/Removes enrichment entities from a previous enrichment.
   *
   * @param rdf the RDF to be processed
   */
  void cleanupPreviousEnrichmentEntities(RDF rdf);
}
