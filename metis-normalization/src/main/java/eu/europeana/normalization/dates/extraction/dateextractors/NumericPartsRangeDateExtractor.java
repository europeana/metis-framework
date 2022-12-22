package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;

/**
 * Patterns for numeric date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link NumericPartsDateExtractor} code for the edges.</p>
 */
public class NumericPartsRangeDateExtractor implements DateExtractor {

  private static final NumericPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericPartsDateExtractor();

  /**
   * Extract the date normalization result for a range.
   * <p>
   * The date is split in two edges using the {@link NumericRangeDateDelimiters#values()} as a separator. The result will contain
   * the first split that is exactly splitting the original value in two parts(edges) and those two edge are valid parsable edges
   * or null if none found.
   * </p>
   *
   * @param inputValue the range value to attempt parsing
   * @return the date normalization result
   */
  public DateNormalizationResult extract(String inputValue) {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    DateNormalizationResult startDate;
    DateNormalizationResult endDate;
    DateNormalizationResult rangeDate = null;
    for (NumericRangeDateDelimiters numericRangeSpecialCharacters : NumericRangeDateDelimiters.values()) {
      // Split with -1 limit does not discard empty splits
      final String[] sanitizedDateSplitArray = sanitizedValue.split(numericRangeSpecialCharacters.getDatesSeparator(), -1);
      // The sanitizedDateSplitArray has to be exactly in two, and then we can verify.
      // This also guarantees that the separator used is not used for unknown characters.
      if (sanitizedDateSplitArray.length == 2) {
        // Try extraction and verify
        startDate = extractDateNormalizationResult(sanitizedDateSplitArray[0], numericRangeSpecialCharacters);
        endDate = extractDateNormalizationResult(sanitizedDateSplitArray[1], numericRangeSpecialCharacters);
        if (startDate != null && endDate != null && !areYearsAmbiguous((InstantEdtfDate) startDate.getEdtfDate(),
            (InstantEdtfDate) endDate.getEdtfDate(),
            numericRangeSpecialCharacters)) {

          final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId =
              getDateNormalizationExtractorId(startDate, endDate);
          final IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate((InstantEdtfDate) startDate.getEdtfDate(),
              (InstantEdtfDate) endDate.getEdtfDate());
          rangeDate = new DateNormalizationResult(dateNormalizationExtractorMatchId, inputValue, intervalEdtfDate);
          break;
        }
      }
    }
    return rangeDate;
  }

  /**
   * Captures the ambiguous case of "198-?".
   *
   * @param startDate the start date of a range
   * @param endDate the end date of the range
   * @param numericRangeSpecialCharacters the date separator of the range
   * @return true if the range is ambiguous
   */
  private boolean areYearsAmbiguous(InstantEdtfDate startDate, InstantEdtfDate endDate,
      NumericRangeDateDelimiters numericRangeSpecialCharacters) {
    boolean isAmbiguous = false;
    if (numericRangeSpecialCharacters == NumericRangeDateDelimiters.DASH_RANGE) {
      final boolean isStartSpecified = !startDate.getEdtfDatePart().isUnspecified();
      final boolean isStartThreeDigit = isStartSpecified && startDate.getEdtfDatePart().getYear().toString().matches("\\d{3}");
      if (isStartThreeDigit && endDate.isUnspecified()) {
        isAmbiguous = true;
      }
    }
    return isAmbiguous;
  }

  private DateNormalizationResult extractDateNormalizationResult(String dateString,
      NumericRangeDateDelimiters numericRangeSpecialCharacters) {
    final DateNormalizationResult dateNormalizationResult;
    if (numericRangeSpecialCharacters.getUnspecifiedCharacters() != null && dateString.matches(
        numericRangeSpecialCharacters.getUnspecifiedCharacters())) {
      dateNormalizationResult = new DateNormalizationResult(NUMERIC_ALL_VARIANTS, dateString,
          new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance()));
    } else {
      dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(dateString,
          NumericPartsPattern.NUMERIC_RANGE_SET);
    }
    return dateNormalizationResult;
  }

  private static DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDate,
      DateNormalizationResult endDate) {
    final boolean isStartXX = startDate.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    final boolean isEndXX = endDate.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    return isStartXX || isEndXX ? NUMERIC_RANGE_ALL_VARIANTS_XX : NUMERIC_RANGE_ALL_VARIANTS;
  }
}
