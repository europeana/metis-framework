package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.isTrue;

import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * The language breakdown
 */
public class LanguageBreakdown implements TierProvider<Tier> {

  private final int qualifiedElements;
  private final int qualifiedElementsWithLanguage;
  private final float qualifiedElementsWithLanguagePercentage;
  private final List<String> qualifiedElementsWithoutLanguageList;
  private final Tier tier;

  /**
   * Constructor with required parameters.
   *
   * @param qualifiedElements the qualified elementes
   * @param qualifiedElementsWithoutLanguageList the qualified elements that do not contain a language
   * @param tier the tier for the breakdown
   */
  @SuppressWarnings("java:S2164") // We don't need double precision here
  public LanguageBreakdown(int qualifiedElements, List<String> qualifiedElementsWithoutLanguageList, Tier tier) {
    this.qualifiedElementsWithoutLanguageList =
        qualifiedElementsWithoutLanguageList == null ? new ArrayList<>() : new ArrayList<>(qualifiedElementsWithoutLanguageList);

    //Sanity check
    isTrue(qualifiedElements >= this.qualifiedElementsWithoutLanguageList.size());
    this.qualifiedElements = qualifiedElements;
    this.qualifiedElementsWithLanguage = qualifiedElements - this.qualifiedElementsWithoutLanguageList.size();
    this.qualifiedElementsWithLanguagePercentage =
        (qualifiedElements == 0) ? 0F : ((qualifiedElementsWithLanguage * 100F) / qualifiedElements);
    this.tier = tier;
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

  public List<String> getQualifiedElementsWithoutLanguageList() {
    return new ArrayList<>(qualifiedElementsWithoutLanguageList);
  }

  @Override
  public Tier getTier() {
    return tier;
  }
}
