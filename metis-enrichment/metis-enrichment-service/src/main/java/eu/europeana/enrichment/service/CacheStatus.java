package eu.europeana.enrichment.service;

import java.util.Arrays;

public enum CacheStatus {

  STARTED("The process to regenerate the cache has started but not finished."),
  FINISHED("The process to regenerate the cache has been completed and the cache is functional."),
  TRIGGERED("The cache is functional, the process to regenerate the cache will be triggered upon"
          + " the next redeploy/restart."),
  NONE("No status is known, the cache needs to be regenerated.");

  private final String explanation;

  CacheStatus(String explanation) {
    this.explanation = explanation;
  }

  public String getExplanation() {
    return explanation;
  }

  public static CacheStatus getByName(String name) {
    return Arrays.stream(CacheStatus.values()).filter(status -> status.name().equals(name))
            .findAny().orElse(NONE);
  }
}
