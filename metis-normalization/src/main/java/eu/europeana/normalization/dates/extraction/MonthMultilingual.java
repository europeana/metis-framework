package eu.europeana.normalization.dates.extraction;

import static java.util.Collections.unmodifiableSet;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
        languageValues.add(month.getDisplayName(TextStyle.SHORT, europeanLanguage.getLocale())
                                .toLowerCase(europeanLanguage.getLocale()));
        languageValues.add(month.getDisplayName(TextStyle.SHORT_STANDALONE, europeanLanguage.getLocale())
                                .toLowerCase(europeanLanguage.getLocale()));
        languageValues.add(month.getDisplayName(TextStyle.FULL, europeanLanguage.getLocale())
                                .toLowerCase(europeanLanguage.getLocale()));
        languageValues.add(month.getDisplayName(TextStyle.FULL_STANDALONE, europeanLanguage.getLocale())
                                .toLowerCase(europeanLanguage.getLocale()));
      }
      monthToAllLanguagesStringsMap.put(month, unmodifiableSet(languageValues));
    }
  }

  public Map<Month, Set<String>> getMonthToAllLanguagesStringsMap() {
    return Collections.unmodifiableMap(monthToAllLanguagesStringsMap);
  }

  /**
   * Get {@link Month} by name.
   *
   * @param monthName the month name
   * @return the month
   */
  public Month getMonth(String monthName) {
    return monthToAllLanguagesStringsMap.entrySet().stream()
                                        .filter(entry -> entry.getValue().contains(monthName.toLowerCase(Locale.ROOT)))
                                        .findFirst().map(Entry::getKey).orElse(null);
  }

}
