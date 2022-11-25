package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Dereferencing service Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

  /**
   * Dereference a URI
   *
   * @param resourceId The resource ID (URI) to dereference
   * @return Pair of the dereferenced entity (or multiple in case of parent entities). List is not null, but could be empty and the
   * dereference result status of enrichment. If an exception occurs the status is not set, it should be captured by the callee.
   */
  Pair<List<EnrichmentBase>, DereferenceResultStatus> dereference(String resourceId);
}
