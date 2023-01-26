package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.RDF;

import java.util.Map;
import java.util.Set;

/**
 * Instances of this class can perform dereferencing.
 */
public interface Dereferencer {

  /**
   * Performs the dereference procedure on a RDF
   *
   * @param rdf The RDF to be processed
   * @return A report containing messages of dereference process
   */
  Set<Report> dereference(final RDF rdf);

  /**
   * It gets the dereferenced information and adds it to the RDF using the extracted fields
   *
   * @param resourceIds The extracted fields associated with its class type to add the dereferenced information to the RDF
   * @return DereferencedEntity which contains a list of RDF field names with the information associated with it and a report
   * containing messages of dereference process.
   */
  DereferencedEntity dereferenceEntities(Map<Class<? extends AboutType>,Set<String>> resourceIds);

  /**
   * It extracts the references for dereferencing from a RDF file
   *
   * @param rdf The RDF where the references are extracted from
   * @return A map with a class type associated with a set of extracted references
   */
  Map<Class<? extends AboutType>,Set<String>> extractReferencesForDereferencing(RDF rdf);

}
