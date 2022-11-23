package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Instances of this class can perform dereferencing.
 */
public interface Dereferencer {

  /**
   * Performs the dereference procedure on a RDF
   * @param rdf The RDF to be processed
   * @return A report containing messages of dereference process
   */
  HashSet<ReportMessage> dereference(final RDF rdf);

  /**
   * It gets the dereferenced information and adds it to the RDF using the extracted fields
   * @param resourceIds The extracted fields to add the dereferenced information to the RDF
   * @return Pair of A list of RDF field names with the information associated with it
   * and a report containing messages of dereference process
   */
  Pair<List<EnrichmentBase>, HashSet<ReportMessage>> dereferenceEntities(Set<String> resourceIds);

  /**
   * It extracts the references for dereferencing from a RDF file
   * @param rdf The RDF where the references are extracted from
   * @return A set with the extracted references
   */
  Set<String> extractReferencesForDereferencing(RDF rdf);

}
