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
  private final int distinctEnablingElements;
  private final List<String> metadataGroupsList;
  private final int metadataGroups;
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
    this.distinctEnablingElements = this.distinctEnablingElementsList.size();
    this.metadataGroupsList = metadataGroupsList == null ? new ArrayList<>() : new ArrayList<>(metadataGroupsList);
    this.metadataGroups = this.metadataGroupsList.size();
    this.tier = tier;
  }

  public int getDistinctEnablingElements() {
    return distinctEnablingElements;
  }

  public List<String> getDistinctEnablingElementsList() {
    return new ArrayList<>(distinctEnablingElementsList);
  }

  public int getMetadataGroups() {
    return metadataGroups;
  }

  public List<String> getMetadataGroupsList() {
    return new ArrayList<>(metadataGroupsList);
  }

  @Override
  public Tier getTier() {
    return tier;
  }
}
