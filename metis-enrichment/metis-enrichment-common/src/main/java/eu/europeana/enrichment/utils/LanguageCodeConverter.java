package eu.europeana.enrichment.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LanguageCodeConverter {

  public static final int THREE_CHARACTER_LANGUAGE_LENGTH = 3;
  public static final int TWO_CHARACTER_LANGUAGE_LENGTH = 2;
  private static final Set<String> ALL_2CODE_LANGUAGES;
  private static final Map<String, String> ALL_3CODE_TO_2CODE_LANGUAGES;

  static {
    HashSet<String> all2CodeLanguages = new HashSet<>();
    Map<String, String> all3CodeLanguages = new HashMap<>();
    Arrays.stream(Locale.getISOLanguages()).map(Locale::new).forEach(locale -> {
      all2CodeLanguages.add(locale.getLanguage());
      all3CodeLanguages.put(locale.getISO3Language(), locale.getLanguage());
    });
    ALL_2CODE_LANGUAGES = Collections.unmodifiableSet(all2CodeLanguages);
    ALL_3CODE_TO_2CODE_LANGUAGES = Collections.unmodifiableMap(all3CodeLanguages);
  }

  /**
   * Converts a provided language code.
   * <p>
   * The conversion happens as follows:
   *   <ul>
   *     <li>If the language is of length 3 and it's a valid iso language code then it converts to its equivalent 2 letter iso language code.</li>
   *     <li>If the language is of length 2 and it's a valid iso language code then the same value is returned</li>
   *     <li>If none of the above applies it returns null</li>
   *   </ul>
   * </p>
   *
   * @param inputLanguageCode the input language
   * @return the converted language code
   */
  public String convertLanguageCode(String inputLanguageCode) {
    final String languageCode;
    if (inputLanguageCode != null && inputLanguageCode.length() == THREE_CHARACTER_LANGUAGE_LENGTH) {
      languageCode = ALL_3CODE_TO_2CODE_LANGUAGES.get(inputLanguageCode);
    } else if (inputLanguageCode != null && inputLanguageCode.length() == TWO_CHARACTER_LANGUAGE_LENGTH) {
      languageCode = ALL_2CODE_LANGUAGES.contains(inputLanguageCode) ? inputLanguageCode : null;
    } else {
      languageCode = null;
    }
    return languageCode;
  }

}
