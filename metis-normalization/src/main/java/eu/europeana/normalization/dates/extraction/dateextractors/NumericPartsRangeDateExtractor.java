package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateBoundaryType;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;

/**
 * Patterns for numeric date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link NumericPartsDateExtractor} code for the boundaries.</p>
 */
public class NumericPartsRangeDateExtractor extends AbstractDateExtractor {

  private static final NumericPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericPartsDateExtractor();

  /**
   * Extract the date normalization result for a range.
   * <p>
   * The date is split in two boundaries using the {@link NumericRangeDateDelimiters#values()} as a separator. The result will
   * contain the first split that is exactly splitting the original value in two parts(boundaries) and those two boundaries are
   * valid parsable boundaries or null if none found.
   * </p>
   *
   * @param inputValue the range value to attempt parsing
   * @return the date normalization result
   */
  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    DateNormalizationResult startDateResult;
    DateNormalizationResult endDateResult;
    DateNormalizationResult rangeDate = DateNormalizationResult.getNoMatchResult(inputValue);
    for (NumericRangeDateDelimiters numericRangeSpecialCharacters : NumericRangeDateDelimiters.values()) {
      // Split with -1 limit does not discard empty splits
      final String[] sanitizedDateSplitArray = sanitizedValue.split(numericRangeSpecialCharacters.getDatesSeparator(), -1);
      // The sanitizedDateSplitArray has to be exactly in two, and then we can verify.
      // This also guarantees that the separator used is not used for unknown characters.
      if (sanitizedDateSplitArray.length == 2) {
        // Try extraction and verify
        startDateResult = extractDateNormalizationResult(sanitizedDateSplitArray[0], numericRangeSpecialCharacters,
            requestedDateQualification,
            flexibleDateBuild);
        endDateResult = extractDateNormalizationResult(sanitizedDateSplitArray[1], numericRangeSpecialCharacters,
            requestedDateQualification, flexibleDateBuild);
        if (startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
            && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
            && !areYearsAmbiguous((InstantEdtfDate) startDateResult.getEdtfDate(), (InstantEdtfDate) endDateResult.getEdtfDate(),
            numericRangeSpecialCharacters)) {

          final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId =
              getDateNormalizationExtractorId(startDateResult, endDateResult);
          final IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDateBuilder((InstantEdtfDate) startDateResult.getEdtfDate(),
              (InstantEdtfDate) endDateResult.getEdtfDate()).withFlexibleDateBuild(flexibleDateBuild).build();
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
      final boolean isStartDeclared = startDate.getDateBoundaryType() == DateBoundaryType.DECLARED;
      final boolean isStartThreeDigit =
          isStartDeclared && Integer.toString(startDate.getYear().getValue()).matches("\\d{3}");
      if (isStartThreeDigit && endDate.getDateBoundaryType() == DateBoundaryType.OPEN) {
        isAmbiguous = true;
      }
    }
    return isAmbiguous;
  }

  private DateNormalizationResult extractDateNormalizationResult(String dateString,
      NumericRangeDateDelimiters numericRangeSpecialCharacters, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateExtractionException {
    final DateNormalizationResult dateNormalizationResult;
    if (numericRangeSpecialCharacters.getUnspecifiedCharacters() != null && dateString.matches(
        numericRangeSpecialCharacters.getUnspecifiedCharacters())) {
      dateNormalizationResult = new DateNormalizationResult(NUMERIC_ALL_VARIANTS, dateString, InstantEdtfDate.getOpenInstance());
    } else {
      dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(dateString, requestedDateQualification,
          NumericPartsPattern.NUMERIC_RANGE_SET, allowSwitchMonthDay);
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
