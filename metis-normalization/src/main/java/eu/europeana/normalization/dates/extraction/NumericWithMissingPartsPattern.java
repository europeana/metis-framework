package eu.europeana.normalization.dates.extraction;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.extraction.NumericWithMissingPartsPattern.NumericRangeSpecialCharacters.DASH_RANGE;
import static eu.europeana.normalization.dates.extraction.NumericWithMissingPartsPattern.NumericRangeSpecialCharacters.SLASH_RANGE;
import static eu.europeana.normalization.dates.extraction.NumericWithMissingPartsPattern.NumericRangeSpecialCharacters.SPACED_DASH_RANGE;
import static eu.europeana.normalization.dates.extraction.NumericWithMissingPartsPattern.NumericRangeSpecialCharacters.SPACE_RANGE;
import static java.util.Collections.unmodifiableSet;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enum with all the acceptable date patterns used for numeric dates.
 * <p>This is the main general enum. Furthermore the method
 * {@link #generatePattern(String, DateNormalizationExtractorMatchId, int)} can be used to generate other enums and gives more
 * control on the date delimiters used, the option of XX dates and the order of the year, month, day of the date</p>
 */
public enum NumericWithMissingPartsPattern implements NumericPattern {
  YMD(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_XX(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, NUMERIC_ALL_VARIANTS_XX, 3, 2, 1),

  YMD_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_SPACE(SPACE_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_SPACE(SPACE_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_DASH(DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_DASH(DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_SLASH(SLASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_SLASH(SLASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_XX_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 3, 2, 1),

  YMD_XX_SPACE(SPACE_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX_SPACE(SPACE_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 3, 2, 1),

  YMD_XX_DASH(DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX_DASH(DASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 3, 2, 1),

  YMD_XX_SLASH(SLASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX_SLASH(SLASH_RANGE.getDatesDelimiters(), NUMERIC_ALL_VARIANTS_XX, 3, 2, 1);

  public static final Set<NumericWithMissingPartsPattern> NUMERIC_SET = unmodifiableSet(EnumSet.of(YMD, DMY, YMD_XX, DMY_XX));
  public static final Set<NumericWithMissingPartsPattern> NUMERIC_RANGE_SET = unmodifiableSet(EnumSet.of(
      YMD_SPACED_DASH, DMY_SPACED_DASH,
      YMD_SPACE, DMY_SPACE,
      YMD_DASH, DMY_DASH,
      YMD_SLASH, DMY_SLASH,
      YMD_XX_SPACED_DASH,
      DMY_XX_SPACED_DASH,
      YMD_XX_SPACE, DMY_XX_SPACE,
      YMD_XX_DASH, DMY_XX_DASH,
      YMD_XX_SLASH, DMY_XX_SLASH));
  private static final String DEFAULT_DELIMITERS = "[\\-./]";
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  private static final String YEAR = "(\\d{3}(?!\\?)|\\d{4})";
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  private static final String YEAR_XX = "(\\d{2}(?:XX|UU|--|\\?\\?)|\\d{3}(?!\\?)[XU]|\\d{4})";

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericWithMissingPartsPattern(String dateDelimiters, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      int yearIndex, int monthIndex, int dayIndex) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = yearIndex;
    this.monthIndex = monthIndex;
    this.dayIndex = dayIndex;

    this.pattern = NumericWithMissingPartsPattern.generatePattern(dateDelimiters, dateNormalizationExtractorMatchId,
        yearIndex);
  }

  // TODO: 28/09/2022 Perhaps the missing and XX can be combined in one and then identified from the UNKNOWN_CHARACTERS cleanup??
  private static Pattern generatePattern(String dateDelimiters,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId, int yearIndex) {
    final String optionalQuestionMark = "\\??";
    final String year;
    final String delimiterDigits;
    final String digitsDelimiter;
    if (dateNormalizationExtractorMatchId == NUMERIC_ALL_VARIANTS) {
      year = YEAR;
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{1,2}))?";
      digitsDelimiter = "(?:(\\d{1,2})" + dateDelimiters + ")?";
    } else {
      year = YEAR_XX;
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{2}|XX|UU|(?<!-)--|\\?\\?))?";
      digitsDelimiter = "(?:(\\d{2}|XX|UU|--(?!-)|\\?\\?)" + dateDelimiters + ")?";
    }

    final String dateRegex;
    if (yearIndex == 1) {
      dateRegex = year + delimiterDigits + delimiterDigits;
    } else {
      dateRegex = digitsDelimiter + digitsDelimiter + year;
    }

    return compile(optionalQuestionMark + dateRegex + optionalQuestionMark, CASE_INSENSITIVE);
  }


  public Pattern getPattern() {
    return pattern;
  }

  public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
    return dateNormalizationExtractorMatchId;
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

  /**
   * Enum that contains the special characters(dates separator, dates delimiters, unspecified range edge) for each range
   * separator.
   * <p>
   * Each separator has an entry and each accepts its own date delimiters as well as the unspecified characters supported for
   * edges.
   * </p>
   */
  public enum NumericRangeSpecialCharacters {
    //"[XU]" with "-" delimiter, "[\\-XU]" with "./" delimiters
    SPACED_DASH_RANGE(" - ", DEFAULT_DELIMITERS, Constants.DEFAULT_UNSPECIFIED_CHARACTERS),
    //"[XU]" with "-" delimiter, "[\\-XU]" with "./" delimiters
    PIPE_RANGE("\\|", DEFAULT_DELIMITERS, Constants.DEFAULT_UNSPECIFIED_CHARACTERS),
    //For space separator we don't accept unspecified edges
    //Does not exist in XX
    SPACE_RANGE(" ", DEFAULT_DELIMITERS, null),
    //"[XU]"
    DASH_RANGE("-", "[./]", "\\?|\\.\\."),
    //"[XU]" with "-" delimiter, "[\\-XU]" with "." delimiter
    SLASH_RANGE("/", "[\\-.]", Constants.DEFAULT_UNSPECIFIED_CHARACTERS);

    private final String datesSeparator;
    private final String datesDelimiters;
    private final String unspecifiedCharacters;

    NumericRangeSpecialCharacters(String datesSeparator, String datesDelimiters, String unspecifiedCharacters) {
      this.datesSeparator = datesSeparator;
      this.datesDelimiters = datesDelimiters;
      this.unspecifiedCharacters = unspecifiedCharacters;
    }

    public String getDatesSeparator() {
      return datesSeparator;
    }

    public String getDatesDelimiters() {
      return datesDelimiters;
    }

    public String getUnspecifiedCharacters() {
      return unspecifiedCharacters;
    }

    private static class Constants {

      public static final String DEFAULT_UNSPECIFIED_CHARACTERS = "\\?|-|\\.\\.";
    }
  }
}
