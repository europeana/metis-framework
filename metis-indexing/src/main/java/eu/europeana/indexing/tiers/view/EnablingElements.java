package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnablingElements {

  private final List<String> distinctEnablingElementsList;
  private final int distinctEnablingElements;
  private final List<String> metadataGroupsList;
  private final int metadataGroups;
  private final Tier metadataTier;

  public EnablingElements(List<String> distinctEnablingElementsList, List<String> metadataGroupsList,
      Tier metadataTier) {
    this.distinctEnablingElementsList = Optional.ofNullable(distinctEnablingElementsList)
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toUnmodifiableList());
    distinctEnablingElements = this.distinctEnablingElementsList.size();
    this.metadataGroupsList = Optional.ofNullable(metadataGroupsList)
                                      .stream()
                                      .flatMap(Collection::stream)
                                      .collect(Collectors.toUnmodifiableList());
    metadataGroups = this.metadataGroupsList.size();
    this.metadataTier = metadataTier;
  }

  public int getDistinctEnablingElements() {
    return distinctEnablingElements;
  }

  public List<String> getDistinctEnablingElementsList() {
    return distinctEnablingElementsList;
  }

  public int getMetadataGroups() {
    return metadataGroups;
  }

  public List<String> getMetadataGroupsList() {
    return metadataGroupsList;
  }

  public Tier getMetadataTier() {
    return metadataTier;
  }
}
