package eu.europeana.indexing.tiers.view;

import java.util.List;

public class LanguageBreakdown {

  private int potentialLanguageQualifiedElements;
  private int actualLanguageQualifiedElements;
  private int actualLanguageQualifiedElementsPercentage;
  private int actualLanguageUnqualifiedElements;
  private List<String> actualLanguageUnqualifiedElementsList;
  private String metadataTier;

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

  public int getActualLanguageQualifiedElementsPercentage() {
    return actualLanguageQualifiedElementsPercentage;
  }

  public void setActualLanguageQualifiedElementsPercentage(int actualLanguageQualifiedElementsPercentage) {
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

  public String getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(String metadataTier) {
    this.metadataTier = metadataTier;
  }
}
