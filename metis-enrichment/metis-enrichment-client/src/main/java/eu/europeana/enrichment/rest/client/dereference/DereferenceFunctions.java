package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import java.util.List;
import java.util.Set;

public interface DereferenceFunctions {

  void dereference(final RDF rdf); // TODO: performDereference from EnrichmentWorker

  List<EnrichmentBaseWrapper> dereferenceFields(Set<String> resourceIds); //TODO: dereferenceFields from EnrichmentWorker

  List<EnrichmentBaseWrapper> dereferenceOwnEntities(Set<String> resourceIds); //TODO: dereferenceOwnEntities from EnrichmentWorker

  List<EnrichmentBaseWrapper> dereferenceExternalEntity(String resourceId); //TODO: dereferenceExternalEntity from EnrichmentWorker

  Set<String> extractReferencesForDereferencing(RDF rdf); //TODO: extractReferenceForDereference from EnrichmentWorker

}
