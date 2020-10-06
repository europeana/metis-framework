package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.InputValue;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EnrichmentFunctions {

  void enrichment(final RDF rdf); // TODO: performEnrichment from EnrichmentWorker

  EnrichmentResultList enrichValues(List<InputValue> valuesForEnrichment); // TODO: enrichValues from EnrichmentWorker

  List<EnrichmentBaseWrapper> enrichReferences(Set<String> referencesForEnrichment); // TODO; enrichReferences from EnrichmentWorker

  List<InputValue> extractValuesForEnrichment(RDF rdf); //TODO: extractValuesForEnrichment from EnrichmentWorker

  Map<String, Set<EnrichmentFields>> extractReferencesForEnrichment(RDF rdf); //TODO: extractReferencesForEnrichment from EnrichmentWorker

}
