package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Patterns for numeric dates with variations in the separators of date components, and supporting characters for
 * unknown/unspecified date components.
 */
public class PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor implements DateExtractor {

  ArrayList<Pattern> patterns = new ArrayList<>();
  Pattern cleanSeparatorAndUnknown = Pattern.compile("[-./?X]");
  Pattern unknownChars = Pattern.compile("[u\\-?X]+$", Pattern.CASE_INSENSITIVE);

  Pattern ambigousPattern = Pattern.compile("(\\d\\d\\d-?\\?|\\d\\d\\d-)");

  public PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor() {
    String componentSep = "[./]";
    String dateYmd = "(?<uncertain>\\?)?(?<year>\\d\\dXX|\\d\\duu|\\d\\d--|\\d\\d\\?\\?|\\d\\d\\d[\\d?\\-Xu])"
        + "(" + componentSep + "(?<month>XX|uu|\\d\\d|\\?\\?|--))?(" + componentSep
        + "(?<day>\\d\\d|--|XX|uu|\\?\\?))?(?<uncertain2>\\?)?";
    String dateDmy = "(?<uncertain>\\?)?((?<day>\\d\\d|--|\\?\\?|xx|uu)" + componentSep
        + ")?((?<month>XX|uu|\\d\\d|\\?\\?|--)?" + componentSep
        + ")?(?<year>\\d\\dXX|\\d\\duu|\\d\\d--|\\d\\d\\?\\?|\\d\\d\\d[\\d?\\-Xu])(?<uncertain2>\\?)?";
    patterns.add(Pattern.compile(dateYmd, Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(dateDmy, Pattern.CASE_INSENSITIVE));

    componentSep = "-";
    dateYmd = "(?<uncertain>\\?)?(?<year>\\d\\dXX|\\d\\d\\?\\?|\\d\\d\\d[\\d?X])" + "(" + componentSep
        + "(?<month>XX|\\d\\d|\\?\\?))?(" + componentSep + "(?<day>\\d\\d|XX|\\?\\?))?(?<uncertain2>\\?)?";
    dateDmy = "(?<uncertain>\\?)?((?<day>\\d\\d|--|\\?\\?|xx|uu)" + componentSep
        + ")?((?<month>XX|\\d\\d|\\?\\?)?" + componentSep
        + ")?(?<year>\\d\\dXX|\\d\\d\\?\\?|\\d\\d\\d[\\d\\?X])(?<uncertain2>\\?)?";
    patterns.add(Pattern.compile(dateYmd, Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(dateDmy, Pattern.CASE_INSENSITIVE));
  }

  public DateNormalizationResult extract(String inputValue) {
    for (Pattern pat : patterns) {
      Matcher m = pat.matcher(inputValue);
      if (m.matches()) {
        EdtfDatePart d = new EdtfDatePart();

        String year = m.group("year");
        Matcher mtc = unknownChars.matcher(year);
        if (!mtc.find()) {
          d.setYear(Integer.parseInt(year));
        } else {
          if (mtc.group(0).length() == 2) {
            d.setYearPrecision(YearPrecision.CENTURY);
            d.setYear(Integer.parseInt(year.substring(0, year.length() - mtc.group(0).length())) * 100);
          } else {
            d.setYearPrecision(YearPrecision.DECADE);
            d.setYear(Integer.parseInt(year.substring(0, year.length() - mtc.group(0).length())) * 10);
          }
        }

        String month = m.group("month");
        if (month != null) {
          month = clean(month);
        }
        String day = m.group("day");
        if (day != null) {
          day = clean(day);
        }

        if (!StringUtils.isEmpty(month) && !StringUtils.isEmpty(day)) {
          d.setMonth(safeParse(month));
          d.setDay(safeParse(day));
        } else if (!StringUtils.isEmpty(month)) {
          d.setMonth(safeParse(month));
        } else if (!StringUtils.isEmpty(day)) {
          d.setMonth(safeParse(day));
        }
        if (m.group("uncertain") != null || m.group("uncertain2") != null) {
          d.setUncertain(true);
        }

        Matcher ambigMatcher = ambigousPattern.matcher(inputValue);
        if (ambigMatcher.matches()) {
          return null;// these cases are ambiguous. Examples '187-?', '187?'
        }
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX, inputValue,
            new InstantEdtfDate(d));
      }
    }
    return null;
  }

  private Integer safeParse(String val) {
    try {
      return Integer.parseInt(val);
    } catch (Exception e) {
      return null;
    }
  }

  private String clean(String group) {
    return cleanSeparatorAndUnknown.matcher(group).replaceAll("");
  }
}
