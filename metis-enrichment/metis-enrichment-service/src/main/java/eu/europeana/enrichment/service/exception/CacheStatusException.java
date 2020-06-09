package eu.europeana.enrichment.service.exception;

import eu.europeana.enrichment.service.CacheStatus;

/**
 * Indicates that a certain operation cannot be performed because the cache is not in an appropriate
 * status.
 */
public class CacheStatusException extends Exception {

  public CacheStatusException(CacheStatus currentStatus) {
    super(String.format("Cannot perform this operation when the cache status is equal to %s.",
            currentStatus));
  }
}
