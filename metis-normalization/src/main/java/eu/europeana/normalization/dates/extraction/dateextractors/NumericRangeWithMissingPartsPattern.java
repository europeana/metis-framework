package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.extraction.dateextractors.NumericRangeWithMissingPartsPattern.NumericRangeSpecialCharacters.DASH_RANGE;
import static eu.europeana.normalization.dates.extraction.dateextractors.NumericRangeWithMissingPartsPattern.NumericRangeSpecialCharacters.SLASH_RANGE;
import static eu.europeana.normalization.dates.extraction.dateextractors.NumericRangeWithMissingPartsPattern.NumericRangeSpecialCharacters.SPACED_DASH_RANGE;
import static eu.europeana.normalization.dates.extraction.dateextractors.NumericRangeWithMissingPartsPattern.NumericRangeSpecialCharacters.SPACE_RANGE;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import java.util.regex.Pattern;

/**
 * This enum contains patterns specifically for ranges.
 * <p>Internally the patterns are generated from the {@link NumericWithMissingPartsPattern#generatePattern(String, boolean, int)}
 * using the provided range date delimiters from {@link NumericRangeSpecialCharacters#getDatesDelimiters()}.</p>
 */
// TODO: 01/11/2022 Support XX cases too, with a flag perhaps
enum NumericRangeWithMissingPartsPattern implements NumericPattern {
  YMD_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_SPACED_DASH(SPACED_DASH_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_SPACE(SPACE_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_SPACE(SPACE_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_DASH(DASH_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_DASH(DASH_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_SLASH(SLASH_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY_SLASH(SLASH_RANGE.getDatesDelimiters(), false, NUMERIC_ALL_VARIANTS, 3, 2, 1);

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericRangeWithMissingPartsPattern(String dateDelimiters, boolean isXX,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      int yearIndex, int monthIndex, int dayIndex) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = yearIndex;
    this.monthIndex = monthIndex;
    this.dayIndex = dayIndex;

    this.pattern = NumericWithMissingPartsPattern.generatePattern(dateDelimiters, isXX, yearIndex);
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
   * Enum that contains the special characters(dates separator, dates delimeters, unspecified range edge) for each range
   * separator.
   */
  enum NumericRangeSpecialCharacters {
    SPACED_DASH_RANGE(" - ", "[\\-./]", "\\?|-|\\.\\."),
    SPACE_RANGE(" ", "[\\-./]", ""),
    DASH_RANGE("-", "[./]", "\\?|\\.\\."),
    SLASH_RANGE("/", "[\\-.]", "\\?|-|\\.\\.");

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
