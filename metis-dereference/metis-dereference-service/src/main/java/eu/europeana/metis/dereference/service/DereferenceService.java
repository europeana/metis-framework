package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.DereferenceResult;

/**
 * Dereferencing service Created by ymamakis on 2/11/16.
 */
public interface DereferenceService {

  /**
   * Dereference a URI
   *
   * @param resourceId The resource ID (URI) to dereference
   * @return Dereferenceresult contains of the dereferenced entity (or multiple in case of parent entities). List is not null, but
   * could be empty and the dereference result status of enrichment. If an exception occurs the status is not set, it should be
   * captured by the callee.
   */
  DereferenceResult dereference(String resourceId);
}
