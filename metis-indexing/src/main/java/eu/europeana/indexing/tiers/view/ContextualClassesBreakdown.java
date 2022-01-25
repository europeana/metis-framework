package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.HashSet;
import java.util.Set;

/**
 * The contextual classes breakdown
 */
public class ContextualClassesBreakdown implements TierProvider<Tier> {

  private final int completeContextualResources;
  private final Set<String> distinctClassesList;
  private final Tier tier;

  /**
   * Constructor with required parameters.
   *
   * @param completeContextualResources the complete contextual resources
   * @param distinctClassesList the distinct classes list
   * @param tier the tier for the breakdown
   */
  public ContextualClassesBreakdown(int completeContextualResources, Set<String> distinctClassesList, Tier tier) {
    this.completeContextualResources = completeContextualResources;
    this.distinctClassesList = distinctClassesList == null ? new HashSet<>() : new HashSet<>(distinctClassesList);
    this.tier = tier;
  }

  public int getCompleteContextualResources() {
    return completeContextualResources;
  }

  public Set<String> getDistinctClassesList() {
    return new HashSet<>(distinctClassesList);
  }

  @Override
  public Tier getTier() {
    return tier;
  }
}
