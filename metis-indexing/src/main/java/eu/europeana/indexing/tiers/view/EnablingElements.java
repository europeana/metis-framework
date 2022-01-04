package eu.europeana.indexing.tiers.view;

import java.util.List;

public class EnablingElements {

  private int distinctEnablingElements;
  private List<String> distinctEnablingElementsList;
  private int metadataGroups;
  private List<String> metadataGroupsList;
  private String metadataTier;

  public EnablingElements() {
  }

  public int getDistinctEnablingElements() {
    return distinctEnablingElements;
  }

  public void setDistinctEnablingElements(int distinctEnablingElements) {
    this.distinctEnablingElements = distinctEnablingElements;
  }

  public List<String> getDistinctEnablingElementsList() {
    return distinctEnablingElementsList;
  }

  public void setDistinctEnablingElementsList(List<String> distinctEnablingElementsList) {
    this.distinctEnablingElementsList = distinctEnablingElementsList;
  }

  public int getMetadataGroups() {
    return metadataGroups;
  }

  public void setMetadataGroups(int metadataGroups) {
    this.metadataGroups = metadataGroups;
  }

  public List<String> getMetadataGroupsList() {
    return metadataGroupsList;
  }

  public void setMetadataGroupsList(List<String> metadataGroupsList) {
    this.metadataGroupsList = metadataGroupsList;
  }

  public String getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(String metadataTier) {
    this.metadataTier = metadataTier;
  }
}
