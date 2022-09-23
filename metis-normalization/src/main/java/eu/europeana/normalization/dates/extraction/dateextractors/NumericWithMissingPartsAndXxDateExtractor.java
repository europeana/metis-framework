package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.util.Optional.ofNullable;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Patterns for numeric dates with variations in the separators of date components, and supporting characters for
 * unknown/unspecified date components.
 */
public class NumericWithMissingPartsAndXxDateExtractor implements DateExtractor {
  // TODO: 22/09/2022 Add pattern DMY

  /**
   * The end of the string can be one or three question marks but not two.
   */
  public static final Pattern ENDING_UNCERTAIN_PATTERN = Pattern.compile("(?:(?<!\\?)\\?|\\?{3})$");
  public static final Pattern STARTING_UNCERTAIN_PATTERN = Pattern.compile("^(?:\\?(?!\\?)|\\?{3})");
  public static final String DELIMITERS = "[\\-./]";
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  public static final String YEAR = "(\\d{2}(?:XX|UU|--|\\?\\?)|\\d{3}(?!\\?)[XU]|\\d{4})";
  /**
   * For the double dash case we make sure there isn't a third dash with a lookbehind
   */
  private static final String DELIMITER_DIGITS = "(?:" + DELIMITERS + "(\\d{2}|XX|UU|(?<!-)--|\\?\\?))?";
  private static final String DIGITS_DELIMITER = "(?:(\\d{2}|XX|UU|--(?!-)|\\?\\?)" + DELIMITERS + ")?";
  //  private static final Pattern YMD_PATTERN = Pattern.compile(
  //      "^\\??" + YEAR + DELIMITER_DIGITS + DELIMITER_DIGITS + "\\??$");
  //  private static final Pattern MDY_PATTERN = Pattern.compile(
  //      "^\\??" + DIGITS_DELIMITER + DIGITS_DELIMITER + YEAR + "\\??$");

  private enum NumericWithMissingPartsAndXxPattern {
    YMD(Pattern.compile("^\\??" + YEAR + DELIMITER_DIGITS + DELIMITER_DIGITS + "\\??$"), 1, 2, 3),
    DMY(Pattern.compile("^\\??" + DIGITS_DELIMITER + DIGITS_DELIMITER + YEAR + "\\??$"), 3, 2, 1);

    private final Pattern pattern;
    private final int yearIndex;
    private final int monthIndex;
    private final int dayIndex;

    NumericWithMissingPartsAndXxPattern(Pattern pattern, int yearIndex, int monthIndex, int dayIndex) {
      this.pattern = pattern;
      this.yearIndex = yearIndex;
      this.monthIndex = monthIndex;
      this.dayIndex = dayIndex;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public int getYearIndex() {
      return yearIndex;
    }

    public int getMonthIndex() {
      return monthIndex;
    }

    public int getDayIndex() {
      return dayIndex;
    }
  }

  // TODO: 23/09/2022 to remove
  ArrayList<Pattern> patterns = new ArrayList<>();
  Pattern cleanSeparatorAndUnknown = Pattern.compile("[-./?X]");
  Pattern unknownChars = Pattern.compile("[u\\-?X]+$", Pattern.CASE_INSENSITIVE);

  Pattern ambigousPattern = Pattern.compile("(\\d\\d\\d-?\\?|\\d\\d\\d-)");

  public NumericWithMissingPartsAndXxDateExtractor() {
    String componentSep = "[./]";
    //    String dateYmd = "(?<uncertain>\\?)?(?<year>\\d\\dXX|\\d\\duu|\\d\\d--|\\d\\d\\?\\?|\\d\\d\\d[\\d?\\-Xu])"
    //        + "(" + componentSep + "(?<month>XX|uu|\\d\\d|\\?\\?|--))?(" + componentSep
    //        + "(?<day>\\d\\d|--|XX|uu|\\?\\?))?(?<uncertain2>\\?)?";

    String dateDmy = "(?<uncertain>\\?)?((?<day>\\d\\d|--|\\?\\?|xx|uu)" + componentSep
        + ")?((?<month>XX|uu|\\d\\d|\\?\\?|--)?" + componentSep
        + ")?(?<year>\\d\\dXX|\\d\\duu|\\d\\d--|\\d\\d\\?\\?|\\d\\d\\d[\\d?\\-Xu])(?<uncertain2>\\?)?";
    //    patterns.add(Pattern.compile(dateYmd, Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(dateDmy, Pattern.CASE_INSENSITIVE));

    componentSep = "-";
    //    dateYmd = "(?<uncertain>\\?)?(?<year>\\d\\dXX|\\d\\d\\?\\?|\\d\\d\\d[\\d?X])" + "(" + componentSep
    //        + "(?<month>XX|\\d\\d|\\?\\?))?(" + componentSep + "(?<day>\\d\\d|XX|\\?\\?))?(?<uncertain2>\\?)?";
    dateDmy = "(?<uncertain>\\?)?((?<day>\\d\\d|--|\\?\\?|xx|uu)" + componentSep
        + ")?((?<month>XX|\\d\\d|\\?\\?)?" + componentSep
        + ")?(?<year>\\d\\dXX|\\d\\d\\?\\?|\\d\\d\\d[\\d\\?X])(?<uncertain2>\\?)?";
    //    patterns.add(Pattern.compile(dateYmd, Pattern.CASE_INSENSITIVE));
    patterns.add(Pattern.compile(dateDmy, Pattern.CASE_INSENSITIVE));
  }

  public DateNormalizationResult extract(String inputValue) {
    final String sanitizedValue = inputValue.replaceAll("\\s", " ").trim();
    final boolean uncertain =
        STARTING_UNCERTAIN_PATTERN.matcher(sanitizedValue).find() || ENDING_UNCERTAIN_PATTERN.matcher(sanitizedValue).find();

    DateNormalizationResult dateNormalizationResult = null;
    //    final Matcher matcher = YMD_PATTERN.matcher(sanitizedValue);
    // TODO: 23/09/2022 Fix this to capture all
    final Matcher matcher = NumericWithMissingPartsAndXxPattern.DMY.getPattern().matcher(sanitizedValue);
    if (matcher.matches()) {
      EdtfDatePart edtfDatePart = extractDate(NumericWithMissingPartsAndXxPattern.DMY, matcher, uncertain);
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX, inputValue,
          new InstantEdtfDate(edtfDatePart));
    }
    return dateNormalizationResult;
  }

  private EdtfDatePart extractDate(NumericWithMissingPartsAndXxPattern numericWithMissingPartsAndXxPattern, Matcher matcher,
      boolean uncertain) {
    final String year = matcher.group(numericWithMissingPartsAndXxPattern.getYearIndex());
    final String month = getMonth(numericWithMissingPartsAndXxPattern, matcher);
    final String day = getDay(numericWithMissingPartsAndXxPattern, matcher);
    final EdtfDatePart edtfDatePart = new EdtfDatePart();

    final String yearSanitized = StringUtils.defaultIfEmpty(year.toUpperCase(Locale.US).replaceAll("[XU?-]", ""), "0");
    final String monthSanitized = StringUtils.defaultIfEmpty(month.toUpperCase(Locale.US).replaceAll("[XU?-]", ""), "0");
    final String daySanitized = StringUtils.defaultIfEmpty(day.toUpperCase(Locale.US).replaceAll("[XU?-]", ""), "0");
    final int unknownYearCharacters = year.length() - yearSanitized.length();
    if (unknownYearCharacters == 2) {
      edtfDatePart.setYearPrecision(YearPrecision.CENTURY);
    } else if (unknownYearCharacters == 1) {
      edtfDatePart.setYearPrecision(YearPrecision.DECADE);
    }

    edtfDatePart.setYear(
        Integer.parseInt(yearSanitized) * Optional.ofNullable(edtfDatePart.getYearPrecision()).map(YearPrecision::getDuration)
                                                  .orElse(1));
    edtfDatePart.setMonth(Integer.parseInt(monthSanitized));
    edtfDatePart.setDay(Integer.parseInt(daySanitized));
    edtfDatePart.setUncertain(uncertain);
    return edtfDatePart;
  }

  /**
   * Checks if the month is null and if the day is not null it will get its value instead.
   * <p>That occurs with the DMY matcher when there is no day e.g. 11-1989</p>
   *
   * @param numericWithMissingPartsAndXxPattern the group indices
   * @param matcher the matcher
   * @return the month
   */
  private String getMonth(NumericWithMissingPartsAndXxPattern numericWithMissingPartsAndXxPattern, Matcher matcher) {
    return ofNullable(ofNullable(matcher.group(numericWithMissingPartsAndXxPattern.getMonthIndex()))
        .orElseGet(() -> matcher.group(numericWithMissingPartsAndXxPattern.getDayIndex()))).orElse("0");
  }

  /**
   * Check if month is null, it then returns the default value, otherwise gets the value of day.
   *
   * @param numericWithMissingPartsAndXxPattern the group indices
   * @param matcher the matcher
   * @return the day
   */
  private String getDay(NumericWithMissingPartsAndXxPattern numericWithMissingPartsAndXxPattern, Matcher matcher) {
    return ofNullable(matcher.group(numericWithMissingPartsAndXxPattern.getMonthIndex()))
        .map(optional -> matcher.group(numericWithMissingPartsAndXxPattern.getDayIndex()))
        .orElse("0");
  }

  public DateNormalizationResult extractOld(String inputValue) {
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
