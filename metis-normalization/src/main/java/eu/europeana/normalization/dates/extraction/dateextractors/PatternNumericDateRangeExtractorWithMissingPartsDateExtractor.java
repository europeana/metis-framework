package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patterns for numeric date ranges with variations in the separators of date components
 */
public class PatternNumericDateRangeExtractorWithMissingPartsDateExtractor implements DateExtractor {

  Pattern cleanSeparator = Pattern.compile("[\\-./]");

  ArrayList<Pattern> patterns = new ArrayList<>();

  public PatternNumericDateRangeExtractorWithMissingPartsDateExtractor() {
    String dateSep = "/";
    String componentSep = "[\\-.]";
    String unsepecifiedVals = "\\?|-|\\.\\.";
    String dateYmd = "\\s*((?<year>\\d\\d\\d\\d?)(?<month>" + componentSep + "\\d\\d?)?(?<day>" + componentSep
        + "\\d\\d?)?(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    String dateDmy = "\\s*((?<day>\\d\\d?" + componentSep + ")?(?<month>\\d\\d?" + componentSep
        + ")?(?<year>\\d\\d\\d\\d?)(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    patterns.add(Pattern.compile(dateYmd + dateSep + dateYmd.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));
    patterns.add(Pattern.compile(dateDmy + dateSep + dateDmy.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));

    dateSep = " - ";
    componentSep = "[\\-./]";
    unsepecifiedVals = "\\?|-|\\.\\.";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?)(?<month>" + componentSep + "\\d\\d?)?(?<day>" + componentSep
        + "\\d\\d?)?(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    dateDmy = "\\s*((?<day>\\d\\d?" + componentSep + ")?(?<month>\\d\\d?" + componentSep
        + ")?(?<year>\\d\\d\\d\\d?)(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    patterns.add(Pattern.compile(dateYmd + dateSep + dateYmd.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));
    patterns.add(Pattern.compile(dateDmy + dateSep + dateDmy.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));

    dateSep = "-";
    componentSep = "[./]";
    unsepecifiedVals = "\\?|\\.\\.";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?)(?<month>" + componentSep + "\\d\\d?)?(?<day>" + componentSep
        + "\\d\\d?)?(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    dateDmy = "\\s*((?<day>\\d\\d?" + componentSep + ")?(?<month>\\d\\d?" + componentSep
        + ")?(?<year>\\d\\d\\d\\d?)(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    patterns.add(Pattern.compile(dateYmd + dateSep + dateYmd.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));
    patterns.add(Pattern.compile(dateDmy + dateSep + dateDmy.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));

    dateSep = " ";
    componentSep = "[./\\-]";
    unsepecifiedVals = "";
    dateYmd = "\\s*((?<year>\\d\\d\\d\\d?)(?<month>" + componentSep + "\\d\\d?)?(?<day>" + componentSep
        + "\\d\\d?)?(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    dateDmy = "\\s*((?<day>\\d\\d?" + componentSep + ")?(?<month>\\d\\d?" + componentSep
        + ")?(?<year>\\d\\d\\d\\d?)(?<uncertain>\\?)?|(?<unspecified>" + unsepecifiedVals + "))\\s*";
    patterns.add(Pattern.compile(dateYmd + dateSep + dateYmd.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));
    patterns.add(Pattern.compile(dateDmy + dateSep + dateDmy.replace("year", "year2").replace("month", "month2")
                                                            .replace("day", "day2").replace("uncertain", "uncertain2")
                                                            .replace("unspecified", "unspecified2")));
  }

  public DateNormalizationResult extract(String inputValue) {
    for (Pattern pat : patterns) {
      Matcher m = pat.matcher(inputValue.trim());
      if (m.matches()) {
        EdtfDatePart dStart = new EdtfDatePart();
        if (m.group("unspecified") != null) {
          dStart = EdtfDatePart.getUnspecifiedInstance();
        } else {
          dStart.setYear(Integer.parseInt(m.group("year")));
          if (m.group("month") != null && m.group("day") != null) {
            dStart.setMonth(Integer.parseInt(clean(m.group("month"))));
            dStart.setDay(Integer.parseInt(clean(m.group("day"))));
          } else if (m.group("month") != null) {
            dStart.setMonth(Integer.parseInt(clean(m.group("month"))));
          } else if (m.group("day") != null) {
            dStart.setMonth(Integer.parseInt(clean(m.group("day"))));
          }
          if (m.group("uncertain") != null) {
            dStart.setUncertain(true);
          }
        }
        EdtfDatePart dEnd = new EdtfDatePart();
        if (m.group("unspecified2") != null) {
          dEnd = EdtfDatePart.getUnspecifiedInstance();
        } else {
          dEnd.setYear(Integer.parseInt(m.group("year2")));
          if (m.group("month2") != null && m.group("day2") != null) {
            dEnd.setMonth(Integer.parseInt(clean(m.group("month2"))));
            dEnd.setDay(Integer.parseInt(clean(m.group("day2"))));
          } else if (m.group("month2") != null) {
            dEnd.setMonth(Integer.parseInt(clean(m.group("month2"))));
          } else if (m.group("day2") != null) {
            dEnd.setMonth(Integer.parseInt(clean(m.group("day2"))));
          }
          if (m.group("uncertain2") != null) {
            dEnd.setUncertain(true);
          }
        }

        if (dEnd.isUnspecified() && dStart.getYear() != null && dStart.getYear() < 1000) {
          return null;// these cases are ambiguous. Example '187-?'
        }
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS, inputValue,
            new IntervalEdtfDate(new InstantEdtfDate(dStart), new InstantEdtfDate(dEnd)));
      }
    }
    return null;
  }

  private String clean(String group) {
    return cleanSeparator.matcher(group).replaceFirst("");
  }
}
