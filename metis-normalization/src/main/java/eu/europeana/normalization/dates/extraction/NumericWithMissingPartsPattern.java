package eu.europeana.normalization.dates.extraction;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.YYYY_MM_DD_SPACES;
import static eu.europeana.normalization.dates.extraction.NumericWithMissingPartsPattern.DatePartsIndices.DMY_INDICES;
import static eu.europeana.normalization.dates.extraction.NumericWithMissingPartsPattern.DatePartsIndices.YMD_INDICES;
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
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Enum with all the acceptable date patterns used for numeric dates.
 * <p>This is the main general enum. Furthermore the method
 * {@link #generatePattern(String, DateNormalizationExtractorMatchId, DatePartsIndices)}   can be used to generate other enums and
 * gives more control on the date delimiters used, the option of XX dates and the order of the year, month, day of the date</p>
 */
public enum NumericWithMissingPartsPattern {
  YMD(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_XX(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_SPACES(" ", YMD_INDICES, YYYY_MM_DD_SPACES),
  DMY_SPACES(" ", DMY_INDICES, YYYY_MM_DD_SPACES),

  YMD_SPACED_DASH_RANGE(SPACED_DASH_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_SPACED_DASH_RANGE(SPACED_DASH_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_SPACE_RANGE(SPACE_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_SPACE_RANGE(SPACE_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_DASH_RANGE(DASH_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_DASH_RANGE(DASH_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_SLASH_RANGE(SLASH_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_SLASH_RANGE(SLASH_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_XX_SPACED_DASH_RANGE(SPACED_DASH_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SPACED_DASH_RANGE(SPACED_DASH_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_SPACE_RANGE(SPACE_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SPACE_RANGE(SPACE_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_DASH_RANGE(DASH_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_DASH_RANGE(DASH_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_SLASH_RANGE(SLASH_RANGE.getDatesDelimiters(), YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SLASH_RANGE(SLASH_RANGE.getDatesDelimiters(), DMY_INDICES, NUMERIC_ALL_VARIANTS_XX);

  public static final Set<NumericWithMissingPartsPattern> NUMERIC_SET = unmodifiableSet(EnumSet.of(
      YMD, DMY,
      YMD_XX, DMY_XX,
      YMD_SPACES, DMY_SPACES));
  public static final Set<NumericWithMissingPartsPattern> NUMERIC_RANGE_SET = unmodifiableSet(EnumSet.of(
      YMD_SPACED_DASH_RANGE, DMY_SPACED_DASH_RANGE,
      YMD_SPACE_RANGE, DMY_SPACE_RANGE,
      YMD_DASH_RANGE, DMY_DASH_RANGE,
      YMD_SLASH_RANGE, DMY_SLASH_RANGE,
      YMD_XX_SPACED_DASH_RANGE,
      DMY_XX_SPACED_DASH_RANGE,
      YMD_XX_SPACE_RANGE, DMY_XX_SPACE_RANGE,
      YMD_XX_DASH_RANGE, DMY_XX_DASH_RANGE,
      YMD_XX_SLASH_RANGE, DMY_XX_SLASH_RANGE));
  private static final String DEFAULT_DELIMITERS = "[\\-./]";

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericWithMissingPartsPattern(String dateDelimiters, DatePartsIndices dateFormatIndices,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = dateFormatIndices.tripleIndices.getLeft();
    this.monthIndex = dateFormatIndices.tripleIndices.getMiddle();
    this.dayIndex = dateFormatIndices.tripleIndices.getRight();

    this.pattern = NumericWithMissingPartsPattern.generatePattern(dateDelimiters, dateNormalizationExtractorMatchId,
        dateFormatIndices);
  }

  /**
   * Generates the pattern according to the parameters provided.
   *
   * <p>
   * For the 3 digits we make sure there is no question mark in front, using a lookahead. And for XX date regex and the double
   * dash case we make sure there isn't a third dash with a regex lookbehind for delimiter digits order, and with a regex
   * lookahead for digits delimiter order.
   * </p>
   *
   * @param dateDelimiters the date delimiters for the pattern
   * @param dateNormalizationExtractorMatchId the date normalization extractor id
   * @param dateFormatIndices the indices based on the format of the provided date
   * @return the generated pattern
   */
  private static Pattern generatePattern(String dateDelimiters,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      DatePartsIndices dateFormatIndices) {
    final String year;
    final String delimiterDigits;
    final String digitsDelimiter;

    if (dateNormalizationExtractorMatchId == NUMERIC_ALL_VARIANTS) {
      year = "(\\d{3}(?!\\?)|\\d{4})";
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{1,2}))?";
      digitsDelimiter = "(?:(\\d{1,2})" + dateDelimiters + ")?";
    } else if (dateNormalizationExtractorMatchId == NUMERIC_ALL_VARIANTS_XX) {
      year = "(\\d{2}(?:XX|UU|--|\\?\\?)|\\d{3}(?!\\?)[XU]|\\d{4})";
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{2}|XX|UU|(?<!-)--|\\?\\?))?";
      digitsDelimiter = "(?:(\\d{2}|XX|UU|--(?!-)|\\?\\?)" + dateDelimiters + ")?";
    } else if (dateNormalizationExtractorMatchId == YYYY_MM_DD_SPACES) {
      year = "(\\d{4})";
      delimiterDigits = dateDelimiters + "(\\d{1,2})";
      digitsDelimiter = "(\\d{1,2})" + dateDelimiters;
    } else {
      throw new IllegalArgumentException("Invalid date normalization extractor match id");
    }

    final String dateRegex;
    if (dateFormatIndices == YMD_INDICES) {
      dateRegex = year + delimiterDigits + delimiterDigits;
    } else {
      dateRegex = digitsDelimiter + digitsDelimiter + year;
    }

    final String optionalQuestionMark = "\\??";
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
    SPACED_DASH_RANGE(" - ", DEFAULT_DELIMITERS, NumericRangeSpecialCharacters.DEFAULT_UNSPECIFIED_CHARACTERS),
    //"[XU]" with "-" delimiter, "[\\-XU]" with "./" delimiters
    PIPE_RANGE("\\|", DEFAULT_DELIMITERS, NumericRangeSpecialCharacters.DEFAULT_UNSPECIFIED_CHARACTERS),
    //For space separator we don't accept unspecified edges
    //Does not exist in XX
    SPACE_RANGE(" ", DEFAULT_DELIMITERS, null),
    //"[XU]"
    DASH_RANGE("-", "[./]", "\\?|\\.\\."),
    //"[XU]" with "-" delimiter, "[\\-XU]" with "." delimiter
    SLASH_RANGE("/", "[\\-.]", NumericRangeSpecialCharacters.DEFAULT_UNSPECIFIED_CHARACTERS);

    public static final String DEFAULT_UNSPECIFIED_CHARACTERS = "\\?|-|\\.\\.";

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
  }

  /**
   * Simple internal enum that contains the indices order of a DMY and YMD date formatting.
   */
  enum DatePartsIndices {
    DMY_INDICES(ImmutableTriple.of(3, 2, 1)),
    YMD_INDICES(ImmutableTriple.of(1, 2, 3));

    private final Triple<Integer, Integer, Integer> tripleIndices;

    DatePartsIndices(Triple<Integer, Integer, Integer> tripleIndices) {
      this.tripleIndices = tripleIndices;
    }
  }
}
