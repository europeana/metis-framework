package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * The contextual classes breakdown
 */
public class ContextualClassesBreakdown implements TierProvider<Tier> {

  private final int completeContextualResources;
  private final int distinctClassesTotal;
  private final List<String> distinctClassesList;
  private final Tier tier;

  /**
   * Constructor with required parameters.
   *
   * @param completeContextualResources the complete contextual resources
   * @param distinctClassesList the distinct classes list
   * @param tier the tier for the breakdown
   */
  public ContextualClassesBreakdown(int completeContextualResources, List<String> distinctClassesList, Tier tier) {
    this.completeContextualResources = completeContextualResources;
    this.distinctClassesList = distinctClassesList == null ? new ArrayList<>() : new ArrayList<>(distinctClassesList);
    this.distinctClassesTotal = this.distinctClassesList.size();
    this.tier = tier;
  }

  public int getCompleteContextualResources() {
    return completeContextualResources;
  }

  public int getDistinctClassesTotal() {
    return distinctClassesTotal;
  }

  public List<String> getDistinctClassesList() {
    return new ArrayList<>(distinctClassesList);
  }

  @Override
  public Tier getTier() {
    return tier;
  }
}
