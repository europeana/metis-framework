package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.HashSet;
import java.util.Set;

/**
 * The enabling elements breakdown
 */
public class EnablingElementsBreakdown implements TierProvider<MetadataTier> {

  private final Set<String> distinctEnablingElementsList;
  private final Set<String> metadataGroupsList;
  private final MetadataTier metadataTier;

  /**
   * Constructor with required parameters.
   *
   * @param distinctEnablingElementsList the distinct enabling elements list
   * @param metadataGroupsList the metadata groups list
   * @param metadataTier the tier for the breakdown
   */
  public EnablingElementsBreakdown(Set<String> distinctEnablingElementsList, Set<String> metadataGroupsList,
      MetadataTier metadataTier) {
    this.distinctEnablingElementsList =
        distinctEnablingElementsList == null ? new HashSet<>() : new HashSet<>(distinctEnablingElementsList);
    this.metadataGroupsList = metadataGroupsList == null ? new HashSet<>() : new HashSet<>(metadataGroupsList);
    this.metadataTier = metadataTier;
  }

  public Set<String> getDistinctEnablingElementsList() {
    return new HashSet<>(distinctEnablingElementsList);
  }

  public Set<String> getMetadataGroupsList() {
    return new HashSet<>(metadataGroupsList);
  }

  @Override
  public MetadataTier getMetadataTier() {
    return metadataTier;
  }
}
