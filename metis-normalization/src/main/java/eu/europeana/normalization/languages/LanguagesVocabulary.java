package eu.europeana.normalization.languages;

import java.util.function.Function;

/**
 * List of standard language vocabularies that the Languages NAL allow to normalize into
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public enum LanguagesVocabulary {

  /** ISO 639-1 codes. **/
  ISO_639_1(Language::getIso6391),

  /** ISO 639-2b codes. **/
  ISO_639_2B(Language::getIso6392b),

  /** ISO 639-2t codes. **/
  ISO_639_2T(Language::getIso6392t),

  /** ISO 639-3 codes. **/
  ISO_639_3(Language::getIso6393),

  /** EU language name authority code. See https://open-data.europa.eu/en/data/dataset/language. **/
  LANGUAGES_NAL(Language::getAuthorityCode);

  private final Function<Language, String> codeRetriever;

  LanguagesVocabulary(Function<Language, String> codeRetriever) {
    this.codeRetriever = codeRetriever;
  }

  /**
   * This method obtains the code in this vocabulary that is known for the given language.
   * 
   * @param language The language for which to get the code.
   * @return The code of the given language in this vocabulary, or null if no such code is known.
   */
  public String getCodeForLanguage(Language language) {
    return this.codeRetriever.apply(language);
  }
}
