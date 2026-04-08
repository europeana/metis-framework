package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class can perform dereferencing.
 */
public interface Dereferencer extends AutoCloseable {

  /**
   * The various entity types permitted for a resource reference.
   */
  enum PermittedEntityType {

    /** Only Europeana entities are permitted. **/
    EUROPEANA_ENTITY,

    /** Only external (non-Europeana) entities are permitted. **/
    EXTERNAL_ENTITY,

    /** All entities are permitted. **/
    ANY_ENTITY
  }

  /**
   * Performs the dereference procedure on a RDF
   *
   * @param rdf The RDF to be processed
   * @return A report containing messages of dereference process
   */
  Set<Report> dereference(final RDF rdf);

  /**
   * Dereference entity links from the Europeana entity collection as well as from external sources.
   *
   * @param resourceIds The set of references to dereference, each with an associated permitted
   *                    entity type.
   * @return Object containing a list of RDF field names with any dereferenced entities associated
   * with them as well as a report.
   */
  DereferencedEntities dereferenceEntities(Map<String, PermittedEntityType> resourceIds);

  /**
   * It extracts the references for dereferencing from a RDF file
   *
   * @param rdf The RDF where the references are extracted from
   * @return A set of extracted references, each with an associated permitted entity type.
   */
  Map<String, PermittedEntityType> extractReferencesForDereferencing(RDF rdf);

  /**
   * Dereference entity links from the Europeana entity collection.
   *
   * @param resourceIds The set of references to dereference.
   * @return Object containing a list of RDF field names with any dereferenced entities associated
   * with them as well as a report.
   */
  DereferencedEntities dereferenceEuropeanaEntities(Set<ReferenceTerm> resourceIds);
}
