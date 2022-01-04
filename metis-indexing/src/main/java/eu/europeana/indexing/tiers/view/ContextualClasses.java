package eu.europeana.indexing.tiers.view;

import java.util.List;

public class ContextualClasses {

  private int completeContextualResources;
  private int distinctClassesOfCompleteContextualResources;
  private List<String> distinctClassesList;
  private String metadataTier;

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

  public String getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(String metadataTier) {
    this.metadataTier = metadataTier;
  }
}
