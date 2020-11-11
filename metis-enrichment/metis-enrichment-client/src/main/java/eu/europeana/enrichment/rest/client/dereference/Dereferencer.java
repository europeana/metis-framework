package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import java.util.List;
import java.util.Set;

public interface Dereferencer {

  /**
   * Performs the dereference procedure on a RDF
   * @param rdf The RDF to be processed
   * @throws DereferenceException
   */
  void dereference(final RDF rdf) throws DereferenceException;

  /**
   * It gets the dereferenced information and adds it to the RDF using the extracted fields
   * @param resourceIds The extracted fields to add the dereferenced information to the RDF
   * @return A list of RDF field names with the information associated with it
   * @throws DereferenceException
   */
  List<EnrichmentResultBaseWrapper> dereferenceEntities(Set<String> resourceIds) throws DereferenceException;

  /**
   * It extracts the references for dereferencing from a RDF file
   * @param rdf The RDF where the references are extracted from
   * @return A set with the extracted references
   */
  Set<String> extractReferencesForDereferencing(RDF rdf);

}
