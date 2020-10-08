package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import java.util.List;
import java.util.Set;

public interface Dereferencer {

  //TODO: Javadoc
  void dereference(final RDF rdf) throws DereferenceException;

  //TODO: Javadoc
  List<EnrichmentBaseWrapper> dereferenceEntities(Set<String> resourceIds) throws DereferenceException;

  //TODO: Javadoc
  Set<String> extractReferencesForDereferencing(RDF rdf);

}
