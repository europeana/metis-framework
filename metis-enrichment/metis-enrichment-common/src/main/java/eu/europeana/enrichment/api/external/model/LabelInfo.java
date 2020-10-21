package eu.europeana.enrichment.api.external.model;

import dev.morphia.annotations.Embedded;
import eu.europeana.enrichment.internal.model.AbstractEnrichmentEntity;
import java.util.List;

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
  private List<String> lowerCaseLabel;

  public LabelInfo() {
  }

  public LabelInfo(List<String> lowerCaseLabel, String lang) {
    this.lowerCaseLabel = lowerCaseLabel;
    this.lang = lang;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public List<String> getLowerCaseLabel() {
    return lowerCaseLabel;
  }

  public void setLowerCaseLabel(List<String> lowerCaseLabel) {
    this.lowerCaseLabel = lowerCaseLabel;
  }
}
