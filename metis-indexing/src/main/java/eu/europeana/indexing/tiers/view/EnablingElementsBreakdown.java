package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.HashSet;
import java.util.Set;

/**
 * The enabling elements breakdown
 */
public class EnablingElementsBreakdown implements TierProvider<Tier> {

  private final Set<String> distinctEnablingElementsList;
  private final Set<String> metadataGroupsList;
  private final Tier tier;

  /**
   * Constructor with required parameters.
   *
   * @param distinctEnablingElementsList the distinct enabling elements list
   * @param metadataGroupsList the metadata groups list
   * @param tier the tier for the breakdown
   */
  public EnablingElementsBreakdown(Set<String> distinctEnablingElementsList, Set<String> metadataGroupsList,
      Tier tier) {
    this.distinctEnablingElementsList =
        distinctEnablingElementsList == null ? new HashSet<>() : new HashSet<>(distinctEnablingElementsList);
    this.metadataGroupsList = metadataGroupsList == null ? new HashSet<>() : new HashSet<>(metadataGroupsList);
    this.tier = tier;
  }

  public Set<String> getDistinctEnablingElementsList() {
    return new HashSet<>(distinctEnablingElementsList);
  }

  public Set<String> getMetadataGroupsList() {
    return new HashSet<>(metadataGroupsList);
  }

  @Override
  public Tier getTier() {
    return tier;
  }
}
