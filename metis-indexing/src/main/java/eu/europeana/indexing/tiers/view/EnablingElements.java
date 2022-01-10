package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import java.util.List;

public class EnablingElements {

  private int distinctEnablingElements;
  private List<String> distinctEnablingElementsList;
  private int metadataGroups;
  private List<String> metadataGroupsList;
  private Tier metadataTier;

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

  public Tier getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(Tier metadataTier) {
    this.metadataTier = metadataTier;
  }
}
