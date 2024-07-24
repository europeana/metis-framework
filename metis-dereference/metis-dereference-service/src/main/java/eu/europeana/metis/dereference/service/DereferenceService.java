package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.DereferenceResult;

/**
 * Implementations of this interface are able to dereference resource IDs. If the resource's
 * vocabulary specifies a positive iteration count, this method also repeatedly retrieves the
 * 'broader' resources and returns those as well.
 */
public interface DereferenceService {

  /**
   * <p>
   * This method dereferences a resource. If the resource's vocabulary specifies a positive
   * iteration count, this method also repeatedly retrieves the 'broader' resources and returns
   * those as well.
   * </p>
   * <p>
   * A resource may have references to its 'broader' resources. these resources form a directed
   * graph and the iteration count is the distance from the requested resource. This method performs
   * a breadth-first search through this graph to retrieve all resources within a certain distance
   * from the requested resource. The distance depends on the vocabulary of the main resource.
   * </p>
   *
   * @param resourceId The resource to dereference.
   * @return An object containing the dereferenced resources and the result status of the process.
   */
  DereferenceResult dereference(String resourceId);
}
