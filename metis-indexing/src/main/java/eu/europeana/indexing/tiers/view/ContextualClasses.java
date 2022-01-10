package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import java.util.List;

public class ContextualClasses {

  private int completeContextualResources;
  private int distinctClassesOfCompleteContextualResources;
  private List<String> distinctClassesList;
  private Tier metadataTier;

  public ContextualClasses() {
  }

  public int getCompleteContextualResources() {
    return completeContextualResources;
  }

  public void setCompleteContextualResources(int completeContextualResources) {
    this.completeContextualResources = completeContextualResources;
  }

  public int getDistinctClassesOfCompleteContextualResources() {
    return distinctClassesOfCompleteContextualResources;
  }

  public void setDistinctClassesOfCompleteContextualResources(int distinctClassesOfCompleteContextualResources) {
    this.distinctClassesOfCompleteContextualResources = distinctClassesOfCompleteContextualResources;
  }

  public List<String> getDistinctClassesList() {
    return distinctClassesList;
  }

  public void setDistinctClassesList(List<String> distinctClassesList) {
    this.distinctClassesList = distinctClassesList;
  }

  public Tier getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(Tier metadataTier) {
    this.metadataTier = metadataTier;
  }
}
