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
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeDateDelimiters;
import java.util.List;

/**
 * Patterns for numeric date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link NumericPartsDateExtractor} code for the boundaries.</p>
 */
public class NumericPartsRangeDateExtractor extends AbstractDateExtractor implements
    RangeDateExtractor<NumericRangeDateDelimiters> {

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
    return extractRange(inputValue, requestedDateQualification, flexibleDateBuild);
  }

  @Override
  public boolean isRangeMatchSuccess(NumericRangeDateDelimiters rangeDateDelimiters, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && !areYearsAmbiguous((InstantEdtfDate) startDateResult.getEdtfDate(), (InstantEdtfDate) endDateResult.getEdtfDate(),
        rangeDateDelimiters);
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

  @Override
  public List<NumericRangeDateDelimiters> getDateDelimiters() {
    return List.of(NumericRangeDateDelimiters.values());
  }

  @Override
  public String getDatesSeparator(NumericRangeDateDelimiters dateDelimiters) {
    return dateDelimiters.getDatesSeparator();
  }

  @Override
  public DateNormalizationResult extractDateNormalizationResult(String dateString,
      NumericRangeDateDelimiters rangeDateDelimiters, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final DateNormalizationResult dateNormalizationResult;
    if (rangeDateDelimiters.getUnspecifiedCharacters() != null && dateString.matches(
        rangeDateDelimiters.getUnspecifiedCharacters())) {
      dateNormalizationResult = new DateNormalizationResult(NUMERIC_ALL_VARIANTS, dateString, InstantEdtfDate.getOpenInstance());
    } else {
      dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(dateString, requestedDateQualification,
          NumericPartsPattern.NUMERIC_RANGE_SET, flexibleDateBuild);
    }
    return dateNormalizationResult;
  }

  @Override
  public DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    final boolean isStartXX = startDateResult.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    final boolean isEndXX = endDateResult.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    return isStartXX || isEndXX ? NUMERIC_RANGE_ALL_VARIANTS_XX : NUMERIC_RANGE_ALL_VARIANTS;
  }
}
