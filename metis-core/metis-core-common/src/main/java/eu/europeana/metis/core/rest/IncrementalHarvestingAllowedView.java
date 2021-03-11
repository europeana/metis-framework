package eu.europeana.metis.core.rest;

/**
 * An object wrapping a boolean indicating whether incremental harvesting is allowed.
 */
public class IncrementalHarvestingAllowedView {

  private final boolean incrementalHarvestingAllowed;

  public IncrementalHarvestingAllowedView(boolean incrementalHarvestingAllowed) {
    this.incrementalHarvestingAllowed = incrementalHarvestingAllowed;
  }

  public boolean isIncrementalHarvestingAllowed() {
    return incrementalHarvestingAllowed;
  }
}
