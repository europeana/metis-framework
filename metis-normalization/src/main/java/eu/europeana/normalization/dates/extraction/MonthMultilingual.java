package eu.europeana.normalization.dates.extraction;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that holds the names of months in the official languages of the European Union, including abbreviated forms. Used in the
 * PatternMonthName
 */
public class MonthMultilingual {

  HashMap<Month, HashMap<Language, HashSet<String>>> monthStringsByLanguage = new HashMap<Month, HashMap<Language, HashSet<String>>>();

  public MonthMultilingual() {
    for (Month month : Month.values()) {
      HashMap<Language, HashSet<String>> monthValues = new HashMap<Language, HashSet<String>>();
      monthStringsByLanguage.put(month, monthValues);
      for (Language l : Language.values()) {
        HashSet<String> langValues = new HashSet<String>();
        monthValues.put(l, langValues);
        langValues.add(month.getDisplayName(TextStyle.SHORT, l.getLocale()));
        langValues.add(month.getDisplayName(TextStyle.SHORT_STANDALONE, l.getLocale()));
        langValues.add(month.getDisplayName(TextStyle.FULL, l.getLocale()));
        langValues.add(month.getDisplayName(TextStyle.FULL_STANDALONE, l.getLocale()));
      }
    }

  }

  public Set<String> getAllMonthStrings() {
    Set<String> ret = new HashSet<String>();
    for (Month month : Month.values()) {
      HashMap<Language, HashSet<String>> monthValues = monthStringsByLanguage.get(month);
      for (Language l : Language.values()) {
        ret.addAll(monthValues.get(l));
      }
    }
    return ret;
  }

  public Set<String> getMonthStrings(Month month) {
    Set<String> ret = new HashSet<String>();
    HashMap<Language, HashSet<String>> monthValues = monthStringsByLanguage.get(month);
    for (Language l : Language.values()) {
      ret.addAll(monthValues.get(l));
    }
    return ret;
  }

  public Integer parse(String monthName) {
    for (Month month : Month.values()) {
      HashMap<Language, HashSet<String>> monthValues = monthStringsByLanguage.get(month);
      for (Language l : Language.values()) {
        if (monthValues.get(l).contains(monthName)) {
          return month.getValue();
        }
      }
    }
    return null;
  }

}
