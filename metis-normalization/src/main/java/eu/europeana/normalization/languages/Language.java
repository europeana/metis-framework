package eu.europeana.normalization.languages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Data about a language in NAL. It is a subset of the data available in NAL, containing only the
 * data used for matching and normalizing.
 */
public class Language {

  private String iso6391;
  private String iso6392b;
  private String iso6392t;
  private String iso6393;
  private String authorityCode;
  private final List<LanguageLabel> originalNames = new ArrayList<>();
  private final List<LanguageLabel> alternativeNames = new ArrayList<>();
  private final List<LanguageLabel> labels = new ArrayList<>();

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

  public String getAuthorityCode() {
    return authorityCode;
  }

  public void setAuthorityCode(String authorityCode) {
    this.authorityCode = authorityCode;
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

  public List<LanguageLabel> getOriginalNames() {
    return Collections.unmodifiableList(originalNames);
  }

  public List<LanguageLabel> getAlternativeNames() {
    return Collections.unmodifiableList(alternativeNames);
  }

  public List<LanguageLabel> getLabels() {
    return Collections.unmodifiableList(labels);
  }

  /**
   * @return A set containing all labels that are known for this language, including the original
   *         and alternative names.
   */
  public Set<String> getAllLabels() {
    return Stream.of(originalNames, alternativeNames, labels).flatMap(List::stream)
        .map(LanguageLabel::getLabel).collect(Collectors.toSet());
  }

  /**
   * @return A set containing all labels and codes that are known for this language.
   */
  public Set<String> getAllLabelsAndCodes() {
    final Set<String> result = getAllLabels();
    final Set<String> codes = Stream.of(iso6391, iso6392b, iso6392t, iso6393, authorityCode)
        .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
    result.addAll(codes);
    return result;
  }

  @Override
  public String toString() {
    return "NalLanguage [iso6391=" + iso6391 + ", iso6392b=" + iso6392b + ", iso6392t=" + iso6392t
        + ", iso6393=" + iso6393 + ", authoritycode=" + authorityCode + ", originalNames="
        + originalNames + ", alternativeNames=" + alternativeNames + ", labels=" + labels + "]";
  }

  /**
   * Get the language code for the given target vocabulary.
   * 
   * @param vocabulary The target vocabulary for which to obtain the code.
   * @return The code.
   */
  public String getNormalizedLanguageId(LanguagesVocabulary vocabulary) {
    return vocabulary.getCodeForLanguage(this);
  }

  /**
   * Get the preferred label of this language object that matches the given result language. For
   * instance, this may be requesting the preferred name for the English language in Dutch.
   * 
   * @param resultLanguageCode The result language.
   * @return The label.
   */
  public String getPrefLabel(String resultLanguageCode) {
    final LanguageLabel label =
        Stream.of(originalNames, alternativeNames, labels).flatMap(List::stream)
            .filter(language -> StringUtils.equals(resultLanguageCode, language.getLanguage()))
            .findFirst().orElseGet(() -> originalNames.get(0));
    return label.getLabel();
  }
}
