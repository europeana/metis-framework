package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.InputValue;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Enricher {

  /**
   * Performs the enrichment procedure on a RDF
   * @param rdf The RDF to be processed
   * @throws EnrichmentException
   */
  void enrichment(final RDF rdf) throws EnrichmentException;

  /**
   * Retrieves information to enrich the RDF using extracted values
   * @param valuesForEnrichment The values extracted to enrich the RDF
   * @return A enrichment result list with the information retrieved to enrich the RDF
   * @throws EnrichmentException
   */
  EnrichmentResultList enrichValues(List<InputValue> valuesForEnrichment)
      throws EnrichmentException;

  /**
   * Retrieves information to enrich the RDF using extracted references
   * @param referencesForEnrichment The references extracted to enrich the RDF
   * @return A list with RDF fields retrieved and the information associated with it
   * @throws EnrichmentException
   */
  List<EnrichmentBaseWrapper> enrichReferences(Set<String> referencesForEnrichment)
      throws EnrichmentException;

  /**
   * It extracts values for enrichment for a RDF
   * @param rdf The RDF where the values are extracted from
   * @return A list with values extracted from the RDF
   * in addition with other details associated
   */
  List<InputValue> extractValuesForEnrichment(RDF rdf);


  /**
   * It extracts the references for enrichment from a RDF
   * @param rdf The RDF where the references are extracted from
   * @return The extracted references that mapped to the
   * respective type(s) of reference in which they occur.
   */
  Map<String, Set<EnrichmentFields>> extractReferencesForEnrichment(RDF rdf);

}
