package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;
import java.util.List;

public class LanguageBreakdown {

  private int potentialLanguageQualifiedElements;
  private int actualLanguageQualifiedElements;
  private float actualLanguageQualifiedElementsPercentage;
  private int actualLanguageUnqualifiedElements;
  private List<String> actualLanguageUnqualifiedElementsList;
  private Tier metadataTier;

  public LanguageBreakdown() {
  }

  public int getPotentialLanguageQualifiedElements() {
    return potentialLanguageQualifiedElements;
  }

  public void setPotentialLanguageQualifiedElements(int potentialLanguageQualifiedElements) {
    this.potentialLanguageQualifiedElements = potentialLanguageQualifiedElements;
  }

  public int getActualLanguageQualifiedElements() {
    return actualLanguageQualifiedElements;
  }

  public void setActualLanguageQualifiedElements(int actualLanguageQualifiedElements) {
    this.actualLanguageQualifiedElements = actualLanguageQualifiedElements;
  }

  public float getActualLanguageQualifiedElementsPercentage() {
    return actualLanguageQualifiedElementsPercentage;
  }

  public void setActualLanguageQualifiedElementsPercentage(float actualLanguageQualifiedElementsPercentage) {
    this.actualLanguageQualifiedElementsPercentage = actualLanguageQualifiedElementsPercentage;
  }

  public int getActualLanguageUnqualifiedElements() {
    return actualLanguageUnqualifiedElements;
  }

  public void setActualLanguageUnqualifiedElements(int actualLanguageUnqualifiedElements) {
    this.actualLanguageUnqualifiedElements = actualLanguageUnqualifiedElements;
  }

  public List<String> getActualLanguageUnqualifiedElementsList() {
    return actualLanguageUnqualifiedElementsList;
  }

  public void setActualLanguageUnqualifiedElementsList(List<String> actualLanguageUnqualifiedElementsList) {
    this.actualLanguageUnqualifiedElementsList = actualLanguageUnqualifiedElementsList;
  }

  public Tier getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(Tier metadataTier) {
    this.metadataTier = metadataTier;
  }
}
