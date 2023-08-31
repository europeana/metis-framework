package eu.europeana.normalization.dates.extraction;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_SPACES_VARIANT;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.DatePartsIndices.DMY_INDICES;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.DatePartsIndices.YMD_INDICES;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericDateDelimiters.DASH_DOT_DELIMITERS;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericDateDelimiters.DASH_DOT_SLASH_DELIMITERS;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericDateDelimiters.DOT_SLASH_DELIMITERS;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericDateDelimiters.SPACE_DELIMITER;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters.DASH_RANGE;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters.SLASH_RANGE;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters.SPACED_DASH_RANGE;
import static eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters.SPACE_RANGE;
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
public enum NumericPartsPattern {
  YMD(DASH_DOT_SLASH_DELIMITERS, YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY(DASH_DOT_SLASH_DELIMITERS, DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_XX(DASH_DOT_SLASH_DELIMITERS, YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX(DASH_DOT_SLASH_DELIMITERS, DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_SPACES(SPACE_DELIMITER, YMD_INDICES, NUMERIC_SPACES_VARIANT),
  DMY_SPACES(SPACE_DELIMITER, DMY_INDICES, NUMERIC_SPACES_VARIANT),

  YMD_SPACED_DASH_RANGE(SPACED_DASH_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_SPACED_DASH_RANGE(SPACED_DASH_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_SPACE_RANGE(SPACE_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_SPACE_RANGE(SPACE_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_DASH_RANGE(DASH_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_DASH_RANGE(DASH_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_SLASH_RANGE(SLASH_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS),
  DMY_SLASH_RANGE(SLASH_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS),

  YMD_XX_SPACED_DASH_RANGE(SPACED_DASH_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SPACED_DASH_RANGE(SPACED_DASH_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_SPACE_RANGE(SPACE_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SPACE_RANGE(SPACE_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_DASH_RANGE(DASH_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_DASH_RANGE(DASH_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS_XX),

  YMD_XX_SLASH_RANGE(SLASH_RANGE, YMD_INDICES, NUMERIC_ALL_VARIANTS_XX),
  DMY_XX_SLASH_RANGE(SLASH_RANGE, DMY_INDICES, NUMERIC_ALL_VARIANTS_XX);

  public static final Set<NumericPartsPattern> NUMERIC_SET = unmodifiableSet(EnumSet.of(
      YMD, DMY,
      YMD_XX, DMY_XX,
      YMD_SPACES, DMY_SPACES));
  public static final Set<NumericPartsPattern> NUMERIC_RANGE_SET = unmodifiableSet(EnumSet.of(
      YMD_SPACED_DASH_RANGE, DMY_SPACED_DASH_RANGE,
      YMD_SPACE_RANGE, DMY_SPACE_RANGE,
      YMD_DASH_RANGE, DMY_DASH_RANGE,
      YMD_SLASH_RANGE, DMY_SLASH_RANGE,
      YMD_XX_SPACED_DASH_RANGE,
      DMY_XX_SPACED_DASH_RANGE,
      YMD_XX_SPACE_RANGE, DMY_XX_SPACE_RANGE,
      YMD_XX_DASH_RANGE, DMY_XX_DASH_RANGE,
      YMD_XX_SLASH_RANGE, DMY_XX_SLASH_RANGE));

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericPartsPattern(DateDelimiters dateDelimiters, DatePartsIndices dateFormatIndices,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = dateFormatIndices.getYearIndex();
    this.monthIndex = dateFormatIndices.getMonthIndex();
    this.dayIndex = dateFormatIndices.getDayIndex();

    this.pattern = NumericPartsPattern.generatePattern(dateDelimiters.getDatesDelimiters(), dateNormalizationExtractorMatchId,
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
    } else if (dateNormalizationExtractorMatchId == NUMERIC_SPACES_VARIANT) {
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


  private interface DateDelimiters {
    String getDatesDelimiters();
  }

  enum NumericDateDelimiters implements DateDelimiters {
    DASH_DOT_SLASH_DELIMITERS("[\\-./]"),
    DOT_SLASH_DELIMITERS("[./]"),
    DASH_DOT_DELIMITERS("[\\-.]"),
    SPACE_DELIMITER(" ");

    private final String datesDelimiters;

    NumericDateDelimiters(String datesDelimiters) {
      this.datesDelimiters = datesDelimiters;
    }


    @Override
    public String getDatesDelimiters() {
      return datesDelimiters;
    }
  }

  /**
   * Enum that contains the special characters(dates separator, dates delimiters, unspecified range doundary) for each range
   * separator.
   * <p>
   * Each separator has an entry and each accepts its own date delimiters as well as the unspecified characters supported for
   * boundaries.
   * </p>
   */
  public enum NumericRangeDateDelimiters implements DateDelimiters {
    //"[XU]" with "-" delimiter, "[\\-XU]" with "./" delimiters
    SPACED_DASH_RANGE(" - ", DASH_DOT_SLASH_DELIMITERS, NumericRangeDateDelimiters.DEFAULT_UNSPECIFIED_CHARACTERS),
    //"[XU]" with "-" delimiter, "[\\-XU]" with "./" delimiters
    PIPE_RANGE("\\|", DASH_DOT_SLASH_DELIMITERS, NumericRangeDateDelimiters.DEFAULT_UNSPECIFIED_CHARACTERS),
    //For space separator we don't accept unspecified boundaries
    //Does not exist in XX
    SPACE_RANGE(" ", DASH_DOT_SLASH_DELIMITERS, null),
    //"[XU]"
    DASH_RANGE("-", DOT_SLASH_DELIMITERS, "\\?|\\.\\."),
    //"[XU]" with "-" delimiter, "[\\-XU]" with "." delimiter
    SLASH_RANGE("/", DASH_DOT_DELIMITERS, NumericRangeDateDelimiters.DEFAULT_UNSPECIFIED_CHARACTERS);

    public static final String DEFAULT_UNSPECIFIED_CHARACTERS = "\\?|-|\\.\\.";

    private final String datesSeparator;
    private final String datesDelimiters;
    private final String unspecifiedCharacters;

    NumericRangeDateDelimiters(String datesSeparator, NumericDateDelimiters datesDelimiters, String unspecifiedCharacters) {
      this.datesSeparator = datesSeparator;
      this.datesDelimiters = datesDelimiters.getDatesDelimiters();
      this.unspecifiedCharacters = unspecifiedCharacters;
    }

    public String getDatesSeparator() {
      return datesSeparator;
    }

    @Override
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

    private final Triple<Integer, Integer, Integer> indicesTriple;

    DatePartsIndices(Triple<Integer, Integer, Integer> indicesTriple) {
      this.indicesTriple = indicesTriple;
    }

    public Integer getYearIndex() {
      return indicesTriple.getLeft();
    }

    public Integer getMonthIndex() {
      return indicesTriple.getMiddle();
    }

    public Integer getDayIndex() {
      return indicesTriple.getRight();
    }
  }
}
