package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patterns for numeric dates with variations in the separators of date components
 */
public class PatternNumericDateExtractorWithMissingPartsDateExtractor implements DateExtractor {

  ArrayList<Pattern> patterns = new ArrayList<Pattern>();
  Pattern cleanSeparator = Pattern.compile("[\\-./]");

  Pattern ambigousPattern = Pattern.compile("\\d\\d\\d\\?");

  public PatternNumericDateExtractorWithMissingPartsDateExtractor() {
    String componentSep = "[\\-./]";
    String dateYmd = "\\s*(?<uncertain>\\?)?(?<year>\\d\\d\\d\\d?)" + "(?<month>" + componentSep
        + "\\d\\d?)?(?<day>" + componentSep + "\\d\\d?)?(?<uncertain2>\\?)?\\s*";
    String dateDmy = "\\s*(?<uncertain>\\?)?(?<day>\\d\\d?" + componentSep + ")?(?<month>\\d\\d?" + componentSep
        + ")?(?<year>\\d\\d\\d\\d?)(?<uncertain2>\\?)?\\s*";
    patterns.add(Pattern.compile(dateYmd));
    patterns.add(Pattern.compile(dateDmy));
  }

  public DateNormalizationResult extract(String inputValue) {
    for (Pattern pat : patterns) {
      Matcher m = pat.matcher(inputValue);
      if (m.matches()) {
        EdtfDatePart d = new EdtfDatePart();
        d.setYear(Integer.parseInt(m.group("year")));
        if (m.group("month") != null && m.group("day") != null) {
          d.setMonth(Integer.parseInt(clean(m.group("month"))));
          d.setDay(Integer.parseInt(clean(m.group("day"))));
        } else if (m.group("month") != null) {
          d.setMonth(Integer.parseInt(clean(m.group("month"))));
        } else if (m.group("day") != null) {
          d.setMonth(Integer.parseInt(clean(m.group("day"))));
        }
        if (m.group("uncertain") != null || m.group("uncertain2") != null) {
          d.setUncertain(true);
        }

        Matcher ambigMatcher = ambigousPattern.matcher(inputValue);
        if (ambigMatcher.matches()) {
          return null;// these cases are ambiguous. Example '187?'
        }
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS, inputValue,
            new InstantEdtfDate(d));
      }
    }
    return null;
  }

  private String clean(String group) {
    return cleanSeparator.matcher(group).replaceFirst("");
  }

}
