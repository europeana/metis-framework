package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceOrEnrichException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.InputValue;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Enricher {

  //TODO: Javadoc
  void enrichment(final RDF rdf) throws EnrichmentException; // TODO: performEnrichment from EnrichmentWorker

  //TODO: Javadoc
  List<InputValue> extractValuesForEnrichment(RDF rdf); //TODO: extractValuesForEnrichment from EnrichmentWorker

  //TODO: Javadoc
  Map<String, Set<EnrichmentFields>> extractReferencesForEnrichment(RDF rdf); //TODO: extractReferencesForEnrichment from EnrichmentWorker

}
