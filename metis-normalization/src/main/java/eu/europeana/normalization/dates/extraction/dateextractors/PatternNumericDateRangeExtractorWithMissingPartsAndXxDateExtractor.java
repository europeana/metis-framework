package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Patterns for numeric date ranges with variations in the separators of date components, and supporting characters for
 * unknown/unspecified date components.
 */
public class PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor implements DateExtractor {

  Pattern cleanSeparatorAndUnknown = Pattern.compile("[\\-./?Xu]", Pattern.CASE_INSENSITIVE);
  Pattern unknownChars = Pattern.compile("[\\-?Xu]+$", Pattern.CASE_INSENSITIVE);

  ArrayList<Pattern> patterns = new ArrayList<>();

  public PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor() {
    String dateSep = "\\s*[/|]\\s*";
    String componentSep = "[\\-]";
    String componentMissing = "[Xu]";

    String dateYmd = "\\s*((?<year>\\d\\d\\d\\d?|\\d\\d\\d" + componentMissing + "?|\\d\\d+" + componentMissing
        + componentMissing + "?)(" + componentSep + "(?<month>\\d\\d?|\\d" + componentMissing + "?))?("
        + componentSep + "(?<day>\\d\\d?|\\d" + componentMissing + "?))?|(?<unspecified>\\?))\\s*";
    String dateDmy = "\\s*(((?<day>\\d\\d?|\\d" + componentMissing + "?)" + componentSep + ")?((?<month>\\d\\d?|\\d"
        + componentMissing + "?)" + componentSep + ")?(?<year>\\d\\d\\d\\d?|\\d\\d" + componentMissing
        + componentMissing + "?)|(?<unspecified>\\?))\\s*";
    patterns.add(Pattern.compile(
        dateYmd + dateSep
            + dateYmd.replace("year", "year2").replace("month", "month2").replace("day", "day2")
                     .replace("unspecified", "unspecified2"),
        Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(
        dateDmy + dateSep
            + dateDmy.replace("year", "year2").replace("month", "month2").replace("day", "day2")
                     .replace("unspecified", "unspecified2"),
        Pattern.CASE_INSENSITIVE));

    componentSep = "[\\.]";
    componentMissing = "[\\-Xu]";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?|\\d\\d\\d" + componentMissing + "?|\\d\\d+" + componentMissing
        + componentMissing + "?)(" + componentSep + "(?<month>\\d\\d?|\\d" + componentMissing + "?))?("
        + componentSep + "(?<day>\\d\\d?|\\d" + componentMissing + "?))?|(?<unspecified>\\?))\\s*";
    dateDmy = "\\s*(((?<day>\\d\\d?|\\d" + componentMissing + "?)" + componentSep + ")?((?<month>\\d\\d?|\\d"
        + componentMissing + "?)" + componentSep + ")?(?<year>\\d\\d\\d\\d?|\\d\\d" + componentMissing
        + componentMissing + "?)|(?<unspecified>\\?))\\s*";
    patterns.add(Pattern.compile(
        dateYmd + dateSep
            + dateYmd.replace("year", "year2").replace("month", "month2").replace("day", "day2")
                     .replace("unspecified", "unspecified2"),
        Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(
        dateDmy + dateSep
            + dateDmy.replace("year", "year2").replace("month", "month2").replace("day", "day2")
                     .replace("unspecified", "unspecified2"),
        Pattern.CASE_INSENSITIVE));

    dateSep = "\\s+[\\-|]\\s+";
    componentSep = "[./]";
    componentMissing = "[\\-Xu]";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?|\\d\\d\\d" + componentMissing + "?|\\d\\d+" + componentMissing
        + componentMissing + "?)(" + componentSep + "(?<month>\\d\\d?|\\d" + componentMissing + "?))?("
        + componentSep + "(?<day>\\d\\d?|\\d" + componentMissing + "?))?|(?<unspecified>\\?))\\s*";
    dateDmy = "\\s*(((?<day>\\d\\d?|\\d" + componentMissing + "?)" + componentSep + ")?((?<month>\\d\\d?|\\d"
        + componentMissing + "?)" + componentSep + ")?(?<year>\\d\\d\\d\\d?|\\d\\d" + componentMissing
        + componentMissing + "?)|(?<unspecified>\\?))\\s*";
    patterns.add(Pattern.compile(
        dateYmd + dateSep
            + dateYmd.replace("year", "year2").replace("month", "month2").replace("day", "day2")
                     .replace("unspecified", "unspecified2"),
        Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(
        dateDmy + dateSep
            + dateDmy.replace("year", "year2").replace("month", "month2").replace("day", "day2")
                     .replace("unspecified", "unspecified2"),
        Pattern.CASE_INSENSITIVE));

    dateSep = "\\s+-\\s+";
    componentSep = "[\\-]";
    componentMissing = "[Xu]";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?|\\d\\d\\d" + componentMissing + "?|\\d\\d+" + componentMissing
        + componentMissing + "?)(" + componentSep + "(?<month>\\d\\d?|\\d" + componentMissing + "?))?("
        + componentSep + "(?<day>\\d\\d?|\\d" + componentMissing + "?))?|(?<unspecified>\\?))\\s*";
    dateDmy = "\\s*(((?<day>\\d\\d?|\\d" + componentMissing + "?)" + componentSep + ")?((?<month>\\d\\d?|\\d"
        + componentMissing + "?)" + componentSep + ")?(?<year>\\d\\d\\d\\d?|\\d\\d" + componentMissing
        + componentMissing + "?)|(?<unspecified>\\?))\\s*";
    patterns.add(
        Pattern.compile(
            dateYmd + dateSep + dateYmd.replace("year", "year2").replace("month", "month2")
                                       .replace("day", "day2").replace("unspecified", "unspecified2"),
            Pattern.CASE_INSENSITIVE));
    patterns.add(
        Pattern.compile(
            dateDmy + dateSep + dateDmy.replace("year", "year2").replace("month", "month2")
                                       .replace("day", "day2").replace("unspecified", "unspecified2"),
            Pattern.CASE_INSENSITIVE));

    dateSep = "-";
    componentSep = "[./]";
    componentMissing = "[Xu]";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?|\\d\\d\\d" + componentMissing + "?|\\d\\d+" + componentMissing
        + componentMissing + "?)(" + componentSep + "(?<month>\\d\\d?|\\d" + componentMissing + "?))?("
        + componentSep + "(?<day>\\d\\d?|\\d" + componentMissing + "?))?|(?<unspecified>\\?))\\s*";
    dateDmy = "\\s*(((?<day>\\d\\d?|\\d" + componentMissing + "?)" + componentSep + ")?((?<month>\\d\\d?|\\d"
        + componentMissing + "?)" + componentSep + ")?(?<year>\\d\\d\\d\\d?|\\d\\d" + componentMissing
        + componentMissing + "?)|(?<unspecified>\\?))\\s*";
    patterns.add(
        Pattern.compile(
            dateYmd + dateSep + dateYmd.replace("year", "year2").replace("month", "month2")
                                       .replace("day", "day2").replace("unspecified", "unspecified2"),
            Pattern.CASE_INSENSITIVE));
    patterns.add(
        Pattern.compile(
            dateDmy + dateSep + dateDmy.replace("year", "year2").replace("month", "month2")
                                       .replace("day", "day2").replace("unspecified", "unspecified2"),
            Pattern.CASE_INSENSITIVE));
  }

  public DateNormalizationResult extract(String inputValue) {
    for (Pattern pat : patterns) {
      Matcher m = pat.matcher(inputValue);
      if (m.matches()) {
        EdtfDatePart dStart = new EdtfDatePart();
        if (m.group("unspecified") != null) {
          dStart = EdtfDatePart.getUnspecifiedInstance();
        } else {
          String year = m.group("year");
          Matcher mtc = unknownChars.matcher(year);
          if (!mtc.find()) {
            dStart.setYear(Integer.parseInt(year));
          } else {
            if (mtc.group(0).length() == 2) {
              dStart.setYear(
                  Integer.parseInt(year.substring(0, year.length() - mtc.group(0).length())) * 100);
              dStart.setYearPrecision(YearPrecision.CENTURY);
            } else {
              dStart.setYear(
                  Integer.parseInt(year.substring(0, year.length() - mtc.group(0).length())) * 10);
              dStart.setYearPrecision(YearPrecision.DECADE);
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
            dStart.setMonth(Integer.parseInt(month));
            dStart.setDay(Integer.parseInt(day));
          } else if (!StringUtils.isEmpty(month)) {
            dStart.setMonth(Integer.parseInt(month));
          } else if (!StringUtils.isEmpty(day)) {
            dStart.setMonth(Integer.parseInt(day));
          }
          if (m.group("unspecified") != null) {
            dStart.setUnspecified(true);
          }
        }

        EdtfDatePart dEnd = new EdtfDatePart();
        if (m.group("unspecified2") != null) {
          dEnd = EdtfDatePart.getUnspecifiedInstance();
        } else {
          String year = m.group("year2");
          Matcher mtc = unknownChars.matcher(year);
          if (!mtc.find()) {
            dEnd.setYear(Integer.parseInt(year));
          } else {
            if (mtc.group(0).length() == 2) {
              dEnd.setYear(
                  Integer.parseInt(year.substring(0, year.length() - mtc.group(0).length())) * 100);
              dEnd.setYearPrecision(YearPrecision.CENTURY);
            } else {
              dEnd.setYear(
                  Integer.parseInt(year.substring(0, year.length() - mtc.group(0).length())) * 10);
              dEnd.setYearPrecision(YearPrecision.DECADE);
            }
          }

          String month = m.group("month2");
          if (month != null) {
            month = clean(month);
          }
          String day = m.group("day2");
          if (day != null) {
            day = clean(day);
          }

          if (!StringUtils.isEmpty(month) && !StringUtils.isEmpty(day)) {
            dEnd.setMonth(Integer.parseInt(month));
            dEnd.setDay(Integer.parseInt(day));
          } else if (!StringUtils.isEmpty(month)) {
            dEnd.setMonth(Integer.parseInt(month));
          } else if (!StringUtils.isEmpty(day)) {
            dEnd.setMonth(Integer.parseInt(day));
          }
          if (m.group("unspecified2") != null) {
            dEnd.setUnspecified(true);
          }
        }
        if (dEnd.isUnspecified() && dStart.getYear() != null && dStart.getYear() < 1000) {
          return null;// these cases are ambiguous. Example '187-?'
        }
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX, inputValue,
            new IntervalEdtfDate(new InstantEdtfDate(dStart), new InstantEdtfDate(dEnd)));
      }
    }
    return null;
  }

  private String clean(String group) {
    return cleanSeparatorAndUnknown.matcher(group).replaceAll("");
  }
}
