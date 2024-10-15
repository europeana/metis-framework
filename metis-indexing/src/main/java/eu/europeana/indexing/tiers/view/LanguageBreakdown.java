package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.isTrue;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierProvider;
import java.util.HashSet;
import java.util.Set;

/**
 * The language breakdown
 */
public class LanguageBreakdown implements TierProvider<MetadataTier> {

  private final int qualifiedElements;
  private final int qualifiedElementsWithLanguage;
  private final float qualifiedElementsWithLanguagePercentage;
  private final Set<String> qualifiedElementsWithoutLanguageList;
  private final MetadataTier metadataTier;

  /**
   * Constructor with required parameters.
   *
   * @param qualifiedElements the qualified elements
   * @param qualifiedElementsWithoutLanguageList the qualified elements that do not contain a language
   * @param metadataTier the tier for the breakdown
   */
  @SuppressWarnings("java:S2164") // We don't need double precision here
  public LanguageBreakdown(int qualifiedElements, Set<String> qualifiedElementsWithoutLanguageList, MetadataTier metadataTier) {
    this.qualifiedElementsWithoutLanguageList =
        qualifiedElementsWithoutLanguageList == null ? new HashSet<>() : new HashSet<>(qualifiedElementsWithoutLanguageList);

    //Sanity check
    isTrue(qualifiedElements >= this.qualifiedElementsWithoutLanguageList.size());
    this.qualifiedElements = qualifiedElements;
    this.qualifiedElementsWithLanguage = qualifiedElements - this.qualifiedElementsWithoutLanguageList.size();
    this.qualifiedElementsWithLanguagePercentage =
        (qualifiedElements == 0) ? 0F : ((qualifiedElementsWithLanguage * 100F) / qualifiedElements);
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

  public Set<String> getQualifiedElementsWithoutLanguageList() {
    return new HashSet<>(qualifiedElementsWithoutLanguageList);
  }

  @Override
  public MetadataTier getMetadataTier() {
    return metadataTier;
  }
}
