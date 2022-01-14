package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.isTrue;

import eu.europeana.indexing.tiers.model.Tier;
import java.util.List;

public class LanguageBreakdown {

  private final int qualifiedElements;
  private final int qualifiedElementsWithLanguage;
  private final float qualifiedElementsWithLanguagePercentage;
  private final int qualifiedElementsWithoutLanguage;
  private final List<String> qualifiedElementsWithoutLanguageList;
  private final Tier metadataTier;

  public LanguageBreakdown(int qualifiedElements, List<String> qualifiedElementsWithoutLanguageList, Tier metadataTier) {
    //Sanity check
    isTrue(qualifiedElements >= qualifiedElementsWithoutLanguageList.size());
    this.qualifiedElements = qualifiedElements;
    this.qualifiedElementsWithLanguage = qualifiedElements - qualifiedElementsWithoutLanguageList.size();
    this.qualifiedElementsWithLanguagePercentage =
        qualifiedElements == 0 ? 0 : (float) qualifiedElementsWithLanguage * 100 / qualifiedElements;
    this.qualifiedElementsWithoutLanguage = qualifiedElementsWithoutLanguageList.size();
    this.qualifiedElementsWithoutLanguageList = qualifiedElementsWithoutLanguageList;
    this.metadataTier = metadataTier;
  }

  public int getQualifiedElements() {
    return qualifiedElements;
  }

  public int getQualifiedElementsWithLanguage() {
    return qualifiedElementsWithLanguage;
  }

  public float getQualifiedElementsWithLanguagePercentage() {
    return qualifiedElementsWithLanguagePercentage;
  }

  public int getQualifiedElementsWithoutLanguage() {
    return qualifiedElementsWithoutLanguage;
  }

  public List<String> getQualifiedElementsWithoutLanguageList() {
    return qualifiedElementsWithoutLanguageList;
  }

  public Tier getMetadataTier() {
    return metadataTier;
  }
}
