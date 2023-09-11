package eu.europeana.normalization.dates.extraction.extractors;

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
import eu.europeana.normalization.dates.extraction.NumericPartsPattern.NumericRangeQualifier;
import java.util.List;

/**
 * Patterns for numeric date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link NumericPartsDateExtractor} code for the boundaries.</p>
 */
public class NumericPartsRangeDateExtractor extends AbstractRangeDateExtractor<NumericRangeQualifier> {

  private static final NumericPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericPartsDateExtractor();

  @Override
  public boolean isRangeMatchSuccess(NumericRangeQualifier numericRangeQualifier, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && !areYearsAmbiguous((InstantEdtfDate) startDateResult.getEdtfDate(), (InstantEdtfDate) endDateResult.getEdtfDate(),
        numericRangeQualifier);
  }

  /**
   * Captures the ambiguous case of "198-?".
   *
   * @param startDate the start date of a range
   * @param endDate the end date of the range
   * @param numericRangeQualifier the numeric range qualifier
   * @return true if the range is ambiguous
   */
  private boolean areYearsAmbiguous(InstantEdtfDate startDate, InstantEdtfDate endDate,
      NumericRangeQualifier numericRangeQualifier) {
    boolean isAmbiguous = false;
    if (numericRangeQualifier == NumericRangeQualifier.DASH_RANGE) {
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
  public List<NumericRangeQualifier> getRangeDateQualifiers() {
    return List.of(NumericRangeQualifier.values());
  }

  @Override
  public DateNormalizationResultRangePair extractDateNormalizationResult(String startString,
      String endString, NumericRangeQualifier numericRangeQualifier, DateQualification requestedDateQualification,
      boolean flexibleDateBuild)
      throws DateExtractionException {
    return new DateNormalizationResultRangePair(
        extractDate(startString, numericRangeQualifier, requestedDateQualification, flexibleDateBuild),
        extractDate(endString, numericRangeQualifier, requestedDateQualification, flexibleDateBuild));
  }

  @Override
  public DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    final boolean isStartXX = startDateResult.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    final boolean isEndXX = endDateResult.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    return isStartXX || isEndXX ? NUMERIC_RANGE_ALL_VARIANTS_XX : NUMERIC_RANGE_ALL_VARIANTS;
  }

  private static DateNormalizationResult extractDate(String dateString,
      NumericRangeQualifier numericRangeQualifier, DateQualification requestedDateQualification, boolean flexibleDateBuild)
      throws DateExtractionException {
    final DateNormalizationResult dateNormalizationResult;
    if (numericRangeQualifier.getUnspecifiedCharacters() != null && dateString.matches(
        numericRangeQualifier.getUnspecifiedCharacters())) {
      dateNormalizationResult = new DateNormalizationResult(NUMERIC_ALL_VARIANTS, dateString, InstantEdtfDate.getOpenInstance());
    } else {
      dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(dateString, requestedDateQualification,
          NumericPartsPattern.NUMERIC_RANGE_SET, flexibleDateBuild);
    }
    return dateNormalizationResult;
  }
}
