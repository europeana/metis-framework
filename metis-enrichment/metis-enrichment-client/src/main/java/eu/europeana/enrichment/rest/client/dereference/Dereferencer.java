package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Instances of this class can perform dereferencing.
 */
public interface Dereferencer {

  /**
   * Performs the dereference procedure on a RDF
   * @param rdf The RDF to be processed
   * @throws DereferenceException In case there is a problem with dereferencing.
   */
  HashSet<ReportMessage> dereference(final RDF rdf) throws DereferenceException;

  /**
   * It gets the dereferenced information and adds it to the RDF using the extracted fields
   * @param resourceIds The extracted fields to add the dereferenced information to the RDF
   * @return A list of RDF field names with the information associated with it
   * @throws DereferenceException In case there is a problem with dereferencing.
   */
  List<EnrichmentBase> dereferenceEntities(Set<String> resourceIds) throws DereferenceException;

  /**
   * It extracts the references for dereferencing from a RDF file
   * @param rdf The RDF where the references are extracted from
   * @return A set with the extracted references
   */
  Set<String> extractReferencesForDereferencing(RDF rdf);

}
