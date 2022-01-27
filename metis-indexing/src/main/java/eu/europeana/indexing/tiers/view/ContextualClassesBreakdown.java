package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.HashSet;
import java.util.Set;

/**
 * The contextual classes breakdown
 */
public class ContextualClassesBreakdown implements TierProvider<MetadataTier> {

  private final int completeContextualResources;
  private final Set<String> distinctClassesList;
  private final MetadataTier metadataTier;

  /**
   * Constructor with required parameters.
   *
   * @param completeContextualResources the complete contextual resources
   * @param distinctClassesList the distinct classes list
   * @param metadataTier the tier for the breakdown
   */
  public ContextualClassesBreakdown(int completeContextualResources, Set<String> distinctClassesList, MetadataTier metadataTier) {
    this.completeContextualResources = completeContextualResources;
    this.distinctClassesList = distinctClassesList == null ? new HashSet<>() : new HashSet<>(distinctClassesList);
    this.metadataTier = metadataTier;
  }

  public int getCompleteContextualResources() {
    return completeContextualResources;
  }

  public Set<String> getDistinctClassesList() {
    return new HashSet<>(distinctClassesList);
  }

  @Override
  public MetadataTier getMetadataTier() {
    return metadataTier;
  }
}
