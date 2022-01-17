package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContextualClasses {

  private final int completeContextualResources;
  private final int distinctClassesOfCompleteContextualResources;
  private final List<String> distinctClassesList;
  private final Tier metadataTier;

  public ContextualClasses(int completeContextualResources, List<String> distinctClassesList, Tier metadataTier) {
    this.completeContextualResources = completeContextualResources;
    this.distinctClassesList = Optional.ofNullable(distinctClassesList)
                                       .stream()
                                       .flatMap(Collection::stream)
                                       .collect(Collectors.toUnmodifiableList());
    this.distinctClassesOfCompleteContextualResources = this.distinctClassesList.size();
    this.metadataTier = metadataTier;
  }

  public int getCompleteContextualResources() {
    return completeContextualResources;
  }

  public int getDistinctClassesOfCompleteContextualResources() {
    return distinctClassesOfCompleteContextualResources;
  }

  public List<String> getDistinctClassesList() {
    return distinctClassesList;
  }

  public Tier getMetadataTier() {
    return metadataTier;
  }
}
