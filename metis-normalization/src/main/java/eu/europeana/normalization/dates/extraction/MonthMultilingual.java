package eu.europeana.normalization.dates.extraction;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that contains the map of all months and its string representation(full and short in both standard and stand-alone  forms)
 * in all European languages.
 */
public class MonthMultilingual {

  private final EnumMap<Month, Set<String>> monthToAllLanguagesStringsMap;

  /**
   * Default constructor.
   * <p>
   * Initializes the map with the Months to all languages.
   * </p>
   */
  public MonthMultilingual() {
    monthToAllLanguagesStringsMap = new EnumMap<>(Month.class);

    for (Month month : Month.values()) {
      final HashSet<String> languageValues = new HashSet<>();
      for (EuropeanLanguage europeanLanguage : EuropeanLanguage.values()) {
        languageValues.add(month.getDisplayName(TextStyle.SHORT, europeanLanguage.getLocale()));
        languageValues.add(month.getDisplayName(TextStyle.SHORT_STANDALONE, europeanLanguage.getLocale()));
        languageValues.add(month.getDisplayName(TextStyle.FULL, europeanLanguage.getLocale()));
        languageValues.add(month.getDisplayName(TextStyle.FULL_STANDALONE, europeanLanguage.getLocale()));
      }
      monthToAllLanguagesStringsMap.put(month, languageValues);
    }
  }

  public Map<Month, Set<String>> getMonthToAllLanguagesStringsMap() {
    return Collections.unmodifiableMap(monthToAllLanguagesStringsMap);
  }

  /**
   * Get all languages string values for a month.
   *
   * @param month the month
   * @return the set of all string representations
   */
  public Set<String> getMonthStrings(Month month) {
    return monthToAllLanguagesStringsMap.get(month);
  }

  /**
   * Get the month index based on a month name in any supported language, full or short, standard or stand-alone.
   *
   * @param monthName the month name
   * @return the month index
   */
  public Integer getMonthIndexValue(String monthName) {
    return monthToAllLanguagesStringsMap.entrySet().stream().filter(entry -> entry.getValue().contains(monthName))
                                        .findFirst().map(entry -> entry.getKey().getValue()).orElse(null);
  }

}
