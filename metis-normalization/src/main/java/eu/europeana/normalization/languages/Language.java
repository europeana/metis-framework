package eu.europeana.normalization.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

/**
 * Data about a language in NAL. It is a subset of the data available in NAL, containing only the
 * data used for matching and normalizing.
 */
public class Language {

  private String iso6391 = null;
  private String iso6392b = null;
  private String iso6392t = null;
  private String iso6393 = null;
  private List<LanguageLabel> originalNames = new ArrayList<>();
  private List<LanguageLabel> alternativeNames = new ArrayList<>();
  private List<LanguageLabel> labels = new ArrayList<>();

  public String getIso6391() {
    return iso6391;
  }

  void setIso6391(String iso6391) {
    this.iso6391 = iso6391;
  }

  public String getIso6392b() {
    return iso6392b;
  }

  void setIso6392b(String iso6392b) {
    this.iso6392b = iso6392b;
  }

  public String getIso6392t() {
    return iso6392t;
  }

  void setIso6392t(String iso6392t) {
    this.iso6392t = iso6392t;
  }

  public String getIso6393() {
    return iso6393;
  }

  void setIso6393(String iso6393) {
    this.iso6393 = iso6393;
  }

  void addOriginalNames(List<LanguageLabel> newOriginalNames) {
    this.originalNames.addAll(newOriginalNames);
  }

  void addAlternativeNames(List<LanguageLabel> newAlternativeNames) {
    this.alternativeNames.addAll(newAlternativeNames);
  }

  void addLabels(List<LanguageLabel> newLabels) {
    this.labels.addAll(newLabels);
  }

  /**
   * @return A set containing all labels that are known for this language.
   */
  public Set<String> getAllLabels() {
    return Arrays.asList(originalNames, alternativeNames, labels).stream().flatMap(List::stream)
        .map(LanguageLabel::getLabel).collect(Collectors.toSet());
  }

  /**
   * @return A set containing all labels and codes that are known for this language.
   */
  public Set<String> getAllLabelsAndCodes() {
    final Set<String> result = getAllLabels();
    final Set<String> codes = Arrays.asList(iso6391, iso6392b, iso6392t, iso6393).stream()
        .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
    result.addAll(codes);
    return result;
  }

  @Override
  public String toString() {
    return "NalLanguage [iso6391=" + iso6391 + ", iso6392b=" + iso6392b + ", iso6392t=" + iso6392t
        + ", iso6393=" + iso6393 + ", originalNames=" + originalNames + ", alternativeNames="
        + alternativeNames + ", labels=" + labels + "]";
  }

  /**
   * Get the language code for the given target vocabulary.
   * 
   * @param target The target vocabulary for which to obtain the code.
   * @return The code.
   */
  public String getNormalizedLanguageId(LanguagesVocabulary target) {
    final String code;
    switch (target) {
      case ISO_639_1:
        code = getIso6391();
        break;
      case ISO_639_2B:
        code = getIso6392b();
        break;
      case ISO_639_2T:
        code = getIso6392t();
        break;
      case ISO_639_3:
      case LANGUAGES_NAL:
        code = getIso6393();
        break;
      default:
        throw new IllegalStateException("Unknown target vocabulary.");
    }
    return code;
  }

  /**
   * Get the preferred label of this language that matches the given result language.
   * 
   * @param resultLanguageCode The result language.
   * @return The label.
   */
  public String getPrefLabel(String resultLanguageCode) {
    final LanguageLabel label =
        Arrays.asList(originalNames, alternativeNames, labels).stream().flatMap(List::stream)
            .filter(language -> StringUtils.equals(resultLanguageCode, language.getLanguage()))
            .findFirst().orElse(originalNames.get(0));
    return label.getLabel();
  }
}
