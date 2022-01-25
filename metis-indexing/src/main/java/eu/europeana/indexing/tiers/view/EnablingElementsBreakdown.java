package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * The enabling elements breakdown
 */
public class EnablingElementsBreakdown implements TierProvider<Tier> {

  private final List<String> distinctEnablingElementsList;
  private final List<String> metadataGroupsList;
  private final Tier tier;

  /**
   * Constructor with required parameters.
   *
   * @param distinctEnablingElementsList the distinct enabling elements list
   * @param metadataGroupsList the metadata groups list
   * @param tier the tier for the breakdown
   */
  public EnablingElementsBreakdown(List<String> distinctEnablingElementsList, List<String> metadataGroupsList,
      Tier tier) {
    this.distinctEnablingElementsList =
        distinctEnablingElementsList == null ? new ArrayList<>() : new ArrayList<>(distinctEnablingElementsList);
    this.metadataGroupsList = metadataGroupsList == null ? new ArrayList<>() : new ArrayList<>(metadataGroupsList);
    this.tier = tier;
  }

  public List<String> getDistinctEnablingElementsList() {
    return new ArrayList<>(distinctEnablingElementsList);
  }

  public List<String> getMetadataGroupsList() {
    return new ArrayList<>(metadataGroupsList);
  }

  @Override
  public Tier getTier() {
    return tier;
  }
}
