package eu.europeana.enrichment.api.external.model;

import dev.morphia.annotations.Embedded;
import eu.europeana.enrichment.internal.model.AbstractEnrichmentEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains the labels that come from the preLabel fields of a contextual class.
 * <p>Should contain the
 * {@link AbstractEnrichmentEntity#getPrefLabel()} and {@link AbstractEnrichmentEntity#getAltLabel()}
 * fields in lower case per language.
 * </p>
 *
 * @author Simon Tzanakis
 * @since 2020-08-04
 */
@Embedded
public class LabelInfo {

  private String lang;
  private List<String> lowerCaseLabel = new ArrayList<>();

  public LabelInfo() {
  }

  /**
   * Constructor with all parameters
   *
   * @param lowerCaseLabel the lower case label list
   * @param lang the language of the labels
   */
  public LabelInfo(List<String> lowerCaseLabel, String lang) {
    this.lowerCaseLabel =
        lowerCaseLabel == null ? new ArrayList<>() : new ArrayList<>(lowerCaseLabel);
    this.lang = lang;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public List<String> getLowerCaseLabel() {
    return new ArrayList<>(lowerCaseLabel);
  }

  public void setLowerCaseLabel(List<String> lowerCaseLabel) {
    this.lowerCaseLabel =
        lowerCaseLabel == null ? new ArrayList<>() : new ArrayList<>(lowerCaseLabel);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LabelInfo labelInfo = (LabelInfo) o;
    return Objects.equals(lang, labelInfo.lang) && lowerCaseLabel.equals(labelInfo.lowerCaseLabel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lang, lowerCaseLabel);
  }
}
