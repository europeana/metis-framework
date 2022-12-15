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
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Enum with all the acceptable date patterns used for numeric dates.
 * <p>This is the main general enum. Furthermore the method
 * {@link #generatePattern(String, DateNormalizationExtractorMatchId, Triple)}  can be used to generate other enums and gives more
 * control on the date delimiters used, the option of XX dates and the order of the year, month, day of the date</p>
 */
public enum NumericWithMissingPartsPattern implements NumericPattern {
  YMD(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, getYmdIndices(), NUMERIC_ALL_VARIANTS),
  DMY(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, getDmyIndices(), NUMERIC_ALL_VARIANTS),

  YMD_XX(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, getYmdIndices(), NUMERIC_ALL_VARIANTS_XX),
  DMY_XX(NumericWithMissingPartsPattern.DEFAULT_DELIMITERS, getDmyIndices(), NUMERIC_ALL_VARIANTS_XX),

  YMD_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS),
  DMY_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS),

  YMD_SPACE(SPACE_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS),
  DMY_SPACE(SPACE_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS),

  YMD_DASH(DASH_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS),
  DMY_DASH(DASH_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS),

  YMD_SLASH(SLASH_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS),
  DMY_SLASH(SLASH_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS),

  YMD_XX_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_SPACE(SPACE_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SPACE(SPACE_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_DASH(DASH_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_DASH(DASH_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_SLASH(SLASH_RANGE.getDatesDelimiters(), getYmdIndices(), NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SLASH(SLASH_RANGE.getDatesDelimiters(), getDmyIndices(), NUMERIC_ALL_VARIANTS_XX);

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
  private static final int DAY_INDEX = 3;

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericWithMissingPartsPattern(String dateDelimiters, Triple<Integer, Integer, Integer> dateFormatIndices,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = dateFormatIndices.getLeft();
    this.monthIndex = dateFormatIndices.getMiddle();
    this.dayIndex = dateFormatIndices.getRight();

    this.pattern = NumericWithMissingPartsPattern.generatePattern(dateDelimiters, dateNormalizationExtractorMatchId,
        dateFormatIndices);
  }

  /**
   * Generates the pattern according to the parameters provided.
   * <p>
   * For XX date regex and the double dash case we make sure there isn't a third dash with a regex lookbehind for delimiter digits
   * order, and with a regex lookahead for digits delimiter order.
   * </p>
   *
   * @param dateDelimiters the date delimiters for the pattern
   * @param dateNormalizationExtractorMatchId the date normalization extractor id
   * @param dateFormatIndices the indices based on the format of the provided date
   * @return the generated pattern
   */
  private static Pattern generatePattern(String dateDelimiters,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      Triple<Integer, Integer, Integer> dateFormatIndices) {
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
    if (dateFormatIndices.equals(getYmdIndices())) {
      dateRegex = year + delimiterDigits + delimiterDigits;
    } else {
      dateRegex = digitsDelimiter + digitsDelimiter + year;
    }

    return compile(optionalQuestionMark + dateRegex + optionalQuestionMark, CASE_INSENSITIVE);
  }

  private static Triple<Integer, Integer, Integer> getDmyIndices() {
    return ImmutableTriple.of(DAY_INDEX, 2, 1);
  }

  private static Triple<Integer, Integer, Integer> getYmdIndices() {
    return ImmutableTriple.of(1, 2, DAY_INDEX);
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
}
